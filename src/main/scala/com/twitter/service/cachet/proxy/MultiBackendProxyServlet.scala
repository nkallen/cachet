package com.twitter.service.cachet

import net.lag.logging.Logger
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.util.{HashMap => JHashMap, Map => JMap, Properties}
import scala.collection.mutable.ListBuffer

object BackendsToProxyMap {
  val log = Logger.get
  def apply(backendProps: Properties, backendTimeoutMs: Long , numThreads: Int, soBufferSize: Int,
            w3cPath: String, w3cFilename: String): JMap[String, ProxyServlet] = {
    log.info("backendProps: %s", backendProps)
    val backendMap = new JHashMap[String, ProxyServlet]()
    val hosts = new ListBuffer[String]()
    val names = backendProps.propertyNames
    while (names.hasMoreElements) {
      val key = names.nextElement.asInstanceOf[String]
      if (key.endsWith(".ip")) {
        // Removes the .ip portion to just single in on the hostname.
        hosts += key.substring(0, key.length - 3)
      }
    }

    hosts.foreach { host =>
      // create a ProxyServlet
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

        proxy.init(ip, port, sslPort, backendTimeoutMs, numThreads, true, soBufferSize, w3cPath, w3cFilename)
        backendMap.put(host, proxy)
      } catch {
        case e: NumberFormatException => log.error("unable to create backend for host %s", host)
      }
    }

    backendMap
  }
}

/**
 * Supports multiple backends serving disjoint domains.
 */
class MultiBackendProxyServlet(backendProps: Properties, backendTimeoutMs: Long, numThreads: Int, soBufferSize: Int,
                               w3cPath: String, w3cFilename: String) extends HttpServlet {
  val backendMap = BackendsToProxyMap(backendProps, backendTimeoutMs, numThreads, soBufferSize, w3cPath, w3cFilename)

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val host = request.getHeader("Host")
    val backend = backendMap.get(host)
    if (backend != null) {
      backend.service(request, response)
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No backend found for Request with Hostname %s".format(host))
    }
  }
}
