package com.twitter.service.cachet

import proxy.client.{ApacheHttpClient, ForwardRequest, JettyHttpClient}
import com.twitter.commons.W3CStats
import net.lag.logging.Logger
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.net.ConnectException

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var forwardRequest = null: ForwardRequest
  var host: String = ""
  var port: Int = 0
  var timeout: Long = 0L
  val log = Logger.get

  def init(backend_host: String, backend_port: Int, backend_timeout: Long) {
    this.host = backend_host
    this.port = backend_port
    this.timeout = backend_timeout

    val client = new JettyHttpClient(timeout)
    forwardRequest = new ForwardRequest(client, host, port)
  }

  override def init(c: ServletConfig) {
    config = c
    val _host = config.getInitParameter("backend-host") match {
      case null => "localhost"
      case x: String => x
    }

    val _port = config.getInitParameter("backend-port") match {
      case null => 3000
      case x: String => x.toInt
    }

    val _timeout = config.getInitParameter("backend-timeout") match {
      case null => 1000L
      case x: String => x.toLong
    }

    init(_host, _port, _timeout)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    Stats.w3c.time("rs-response-time") {
      try {
        forwardRequest(request, response)
      } catch {
        case c: ConnectException => {
          log.error("unable to connect to backend")
        }
        case e: NullPointerException => {
          // this is GSE telling us it had a connect timeout.
          log.error("unable to talk to backend due to exception: %s".format(e))
        }
        case e => e.printStackTrace; log.error("uncaught exception: " + e)
      }
    }
  }
}

/**
 * At the end of each request, print the w3c log line and clear the w3c log store.
 */
class LoggingFilter extends Filter {
  private val log = Logger.get // FIXME: use a separate logfile
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    chain.doFilter(request, response)
    log.info(Stats.w3c.log_entry)
    Stats.w3c.clear
  }

  def init(filterConfig: FilterConfig) { /* nothing */ }

  def destroy() { /* nothing */ }
}

class BasicFilter extends Filter {
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    response.getWriter().append("yo").append(" - ")
    chain.doFilter(request, response)
  }

  def init(filterConfig: FilterConfig) { /* nothing */ }

  def destroy() { /* nothing */ }
}
