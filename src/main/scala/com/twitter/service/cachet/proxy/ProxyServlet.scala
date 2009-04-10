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
  private val log = Logger.get

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
    log.info(Stats.w3c.log_entry)
  }
}

class BasicFilter extends Filter {
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    response.getWriter().append("yo").append(" - ")
    chain.doFilter(request, response)
  }

  def init(filterConfig: FilterConfig) { /* nothing */ }

  def destroy() { /* nothing */ }
}
