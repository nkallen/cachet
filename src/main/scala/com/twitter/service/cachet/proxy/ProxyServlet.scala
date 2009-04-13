package com.twitter.service.cachet

import proxy.client.{ApacheHttpClient, ForwardRequest, JettyHttpClient}
import com.twitter.commons.W3CStats
import net.lag.logging.Logger
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var forwardRequest = null: ForwardRequest
  var host: String = ""
  var port: Int = 0
  var timeout: Long = 0L

  override def init(c: ServletConfig) {
    config = c
    host = config.getInitParameter("backend-host") match {
      case null => "localhost"
      case x: String => x
    }

    port = config.getInitParameter("backend-port") match {
      case null => 3000
      case x: String => x.toInt
    }

    timeout = config.getInitParameter("backend-timeout") match {
      case null => 1000L
      case x: String => x.toLong
    }

    val client = new JettyHttpClient(timeout)
    forwardRequest = new ForwardRequest(client, host, port)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    Stats.w3c.time("rs-response-time") {
      forwardRequest(request, response)
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
