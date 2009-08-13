package com.twitter.service.cachet

import net.lag.logging.Logger
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.util.{HashMap => JHashMap, Map => JMap, Properties}
import scala.collection.mutable.ListBuffer

object BackendsToProxyMap {
  val log = Logger.get
  var hosts: List[String] = Nil

  def apply(backendProps: Properties, backendTimeoutMs: Long , numThreads: Int, soBufferSize: Int,
            w3cPath: String, w3cFilename: String): JMap[String, ProxyServlet] = {
    log.info("backendProps: %s", backendProps)
    val backendMap = new JHashMap[String, ProxyServlet]()
    val names = backendProps.propertyNames

    hosts = {
      val hosts = new ListBuffer[String]
      while (names.hasMoreElements) {
        val key = names.nextElement.asInstanceOf[String]
        if (key.endsWith(".ip")) {
          // Removes the .ip portion to just single in on the hostname.
          hosts += key.substring(0, key.length - 3)
        }
      }
      // Sort by the longest hostname first for our wildcard matching.
      hosts.toList.sort((a, b) => a.length > b.length)
    }

    hosts.foreach { host =>
      try {
        val proxy = new ProxyServlet()
        val ip = backendProps.get("%s.ip".format(host)).asInstanceOf[String]
        val port = backendProps.get("%s.port".format(host)).asInstanceOf[String].toInt
        val sslPort = try {
          backendProps.get("%s.ssl-port".format(host)).asInstanceOf[String].toInt
        } catch {
          case e: NumberFormatException => {
            log.warning("no ssl port for host %s on IP %s, using default port 10443", host, ip)
            10443
          }
        }

        log.info("adding proxy %s for host %s with ip = %s port = %s sslPort = %s", proxy, host, ip, port, sslPort)
        proxy.init(ip, port, sslPort, backendTimeoutMs, numThreads, true, soBufferSize, w3cPath, w3cFilename)
        backendMap.put(host, proxy)
      } catch {
        case e: NumberFormatException => log.error("unable to create backend for host %s", host)
      }
    }

    backendMap
  }
}

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
        log.debug("Didn't find backend with exact match for host '%s'. Trying wildcard matching now.", host)
        BackendsToProxyMap.hosts.find(domain => host.endsWith(domain)) match {
          case Some(h) => (h, backendMap.get(h))
          case None => (null, null)
        }
      }
      case servlet: ProxyServlet => (host, servlet)
    }
    log.debug("requestHost = '%s' host = '%s' backendHost = '%s'", requestHost, host, backendHost)
    if (backendHost != null) Stats.w3c.log("x-proxy-id", serv.host)
    serv
  }
}

/**
 * Supports multiple backends serving disjoint domains.
 */
class MultiBackendProxyServlet(backendProps: Properties, backendTimeoutMs: Long, numThreads: Int, soBufferSize: Int,
                               w3cPath: String, w3cFilename: String) extends HttpServlet {
  private val log = Logger.get
  HostRouter.setHosts(BackendsToProxyMap(backendProps, backendTimeoutMs, numThreads, soBufferSize, w3cPath, w3cFilename))

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    log.debug("Received request remoteAddr = %s URL = %s", request.getRemoteAddr(), request.getRequestURL())
    val host = request.getHeader("Host")
    if (host == null) {
      log.error("Returning BAD_REQUEST: No Host found in request from remoteAddr = %s URL = %s", request.getRemoteAddr(), request.getRequestURL())
      Stats.noHostFound()
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No host sent in request")
    } else {
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
}
