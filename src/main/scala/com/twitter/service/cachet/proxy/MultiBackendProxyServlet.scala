package com.twitter.service.cachet

import net.lag.logging.Logger
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.util.{HashMap => JHashMap, Map => JMap, Properties, Set => JSet}
import scala.collection.mutable

object BackendsToProxyMap {
  val log = Logger.get
  var hosts: List[String] = Nil

  def apply(backends: List[ProxyBackendConfig], backendTimeoutMs: Long, numThreads: Int, soBufferSize: Int,
            w3cPath: String, w3cFilename: String, errorStrings: Map[Int, String]): JMap[String, ProxyServlet] = {
    val backendMap = new JHashMap[String, ProxyServlet]()

    hosts = backends.map { config =>
      val proxy = new ProxyServlet()
      val host = config.domain
      proxy.init(host, config.ip, config.port, config.sslPort, backendTimeoutMs, numThreads, true, soBufferSize, w3cPath, w3cFilename, errorStrings)
      log.info("adding proxy %s for host %s with ip = %s port = %s sslPort = %s", proxy.id,
               host, config.ip, config.port, config.sslPort)
      backendMap.put(host, proxy)
      config.aliases.foreach { alias => backendMap.put(alias, proxy) }
      host
    }.sort((a, b) => a.length > b.length)

    backendMap
  }
}

case class ProxyBackendConfig(domain: String, ip: String, port: Int, sslPort: Option[Int], aliases: Seq[String])

object HostRouter {
  private val log = Logger.get
  val backendMap = new JHashMap[String, ProxyServlet]()

  def setHosts(backends: JMap[String, ProxyServlet]) {
    backendMap.putAll(backends)
  }

  /**
   * Adds an alias to the HostRouter for a given host.
   */
  def +=(alias_host: (String, String)) {
    HostRouter.backendMap.get(alias_host._2) match {
      case null => throw new IllegalStateException("no backend found for " + alias_host._2)
      case p: ProxyServlet => backendMap.put(alias_host._1, p)
    }
  }

  def apply(requestHost: String): ProxyServlet = {
    if (requestHost == null) {
      return null
    }
    val host = if (requestHost.contains(":")) {
      requestHost.split(":")(0)
    } else {
      requestHost
    }

    val (backendHost, serv) = backendMap.get(host) match {
      case null => {
        // Wildcard matching. e.g. foo.twitter.com => twitter.com
        log.warning("Didn't find backend with exact match for host '%s'. Trying wildcard matching now.", host)
        BackendsToProxyMap.hosts.find(domain => host.endsWith(domain)) match {
          case Some(h) => (h, backendMap.get(h))
          case None => (null, null)
        }
      }
      case servlet: ProxyServlet => (host, servlet)
    }
    log.debug("requestHost = '%s' host = '%s' backendHost = '%s'", requestHost, host, backendHost)
    serv
  }

  override def toString(): String = {
    val output = new mutable.ListBuffer[String]()
    val backends: JSet[String] = backendMap.keySet
    val iterator = backends.iterator
    while(iterator.hasNext) {
      val alias = iterator.next
      val proxy = backendMap.get(alias)
      output += "%s -> %s".format(alias, proxy.toString)
    }
    output.toList.sort(_ < _).mkString("\n")
  }
}

/**
 * Supports multiple backends serving disjoint domains.
 *
 * @param defaultHost - the hostname to pick if the request doesn't send one.
 */
class MultiBackendProxyServlet(defaultHost: String, backends: List[ProxyBackendConfig], backendTimeoutMs: Long, numThreads: Int,
                               soBufferSize: Int, w3cPath: String, w3cFilename: String, errorStrings: Map[Int, String]) extends HttpServlet {
  private val log = Logger.get
  HostRouter.setHosts(BackendsToProxyMap(backends, backendTimeoutMs, numThreads, soBufferSize, w3cPath, w3cFilename, errorStrings))
  private val defaultBackend = HostRouter(defaultHost)

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    var host = request.getHeader("Host")
    log.debug("MultiBackendProxyServlet: Received New Request with protocol = %s method = %s remoteAddr = %s URL = %s Host = %s",
                  request.getProtocol(), request.getMethod(), request.getRemoteAddr(), request.getRequestURL(), host)
    Stats.w3c.log("host", host)
    var backend = HostRouter(host)
    if (backend == null) {
      // Use default backend - But we don't explicitly set the Host to a defaulthost in the http request, letting
      // the backend deal with the weird host.
      log.warning("Bad request: No backend found for Request: protocol = %s method = %s remoteAddr = %s URL = %s Host = %s",
                  request.getProtocol(), request.getMethod(), request.getRemoteAddr(), request.getRequestURL(), host)
      Stats.noProxyFoundForHost()
      Stats.w3c.log("x-default-backend", "1")
      host = defaultHost
      backend = defaultBackend
    }

    // actually service the request now
    Stats.countRequestsForHost(host)
    try {
      Stats.w3c.log("x-proxy-id", backend.id)
      backend.service(request, response)
    } catch {
      case e => {
        log.error("Unable to service request for unknown reason (Exception = %s, message = %s, cause = %s)  Request: protocol = %s method = %s remoteAddr = %s URL = %s Host = %s", 
          e.toString, e.getMessage(), e.getCause(), request.getProtocol(), request.getMethod(), request.getRemoteAddr(), request.getRequestURL(), host)
        val statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        response.setStatus(statusCode)
        response.getOutputStream.print(errorStrings.getOrElse(statusCode, ""))
        Stats.returned5xx()
      }
    }
  }
}
