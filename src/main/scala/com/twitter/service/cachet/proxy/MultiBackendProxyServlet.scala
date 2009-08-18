package com.twitter.service.cachet

import net.lag.logging.Logger
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.util.{HashMap => JHashMap, Map => JMap, Properties, Set => JSet}
import scala.collection.mutable.ListBuffer

object BackendsToProxyMap {
  val log = Logger.get
  var hosts: List[String] = Nil

  def apply(backends: List[ProxyBackendConfig], backendTimeoutMs: Long, numThreads: Int, soBufferSize: Int,
            w3cPath: String, w3cFilename: String): JMap[String, ProxyServlet] = {
    val backendMap = new JHashMap[String, ProxyServlet]()

    hosts = backends.map { config =>
      val proxy = new ProxyServlet()
      val host = config.domain
      proxy.init(host, config.ip, config.port, config.sslPort, backendTimeoutMs, numThreads, true, soBufferSize, w3cPath, w3cFilename)
      log.info("adding proxy %s for host %s with ip = %s port = %s sslPort = %s", proxy.id,
               host, config.ip, config.port, config.sslPort)
      backendMap.put(host, proxy)
      config.aliases.foreach { alias => HostRouter += alias -> host }
      host
    }.sort((a, b) => a.length > b.length)

    backendMap
  }
}

case class ProxyBackendConfig(domain: String, ip: String, port: Int, sslPort: Int, aliases: Seq[String])

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
    val host = if (requestHost.contains(":")) {
      requestHost.split(":")(0)
    } else {
      requestHost
    }

    Stats.w3c.log("host", host)
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
    if (backendHost != null) Stats.w3c.log("x-proxy-id", serv.id)
    serv
  }

  override def toString(): String = {
    val output = new ListBuffer[String]()
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
                               soBufferSize: Int, w3cPath: String, w3cFilename: String) extends HttpServlet {
  private val log = Logger.get
  HostRouter.setHosts(BackendsToProxyMap(backends, backendTimeoutMs, numThreads, soBufferSize, w3cPath, w3cFilename))

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    log.debug("Received request remoteAddr = %s URL = %s", request.getRemoteAddr(), request.getRequestURL())
    var host = request.getHeader("Host")
    if (host == null || host.length == 0) {
      log.warning("Found null/empty host in request. Host = '%s' RemoteAddr = %s URL = %s Protocol = %s. Setting host to %s",
                  host, request.getRemoteAddr(), request.getRequestURL(), request.getProtocol(), defaultHost)
      host = defaultHost
      Stats.noHostFound()
    }
    val backend = HostRouter(host)

    if (backend != null) {
      Stats.countRequestsForHost(host)
      backend.service(request, response)
    } else {
      log.error("Returning BAD_REQUEST: No backend found for Request for Host %s", host)
      Stats.noProxyFoundForHost()
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No backend found for Request with Hostname %s".format(host))
    }
  }
}
