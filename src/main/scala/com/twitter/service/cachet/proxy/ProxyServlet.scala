package com.twitter.service.cachet

import com.twitter.service.cache.proxy.client.ForwardRequest
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import proxy.client.{JettyHttpClient, ApacheHttpClient}

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
    // FIXME: Add backend-response-time metric.
    forwardRequest(request, response)
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
