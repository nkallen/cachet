package com.twitter.service.cachet.servlet

import com.twitter.service.cache.proxy.client.ForwardRequest
import javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import proxy.client.JettyHttpClient

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var forwardRequest = null: ForwardRequest

  override def init(c: ServletConfig) {
    config = c
    val client = new JettyHttpClient
    forwardRequest = new ForwardRequest(client, config.getInitParameter("host"), config.getInitParameter("port").toInt)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    forwardRequest(request, response)
  }
}