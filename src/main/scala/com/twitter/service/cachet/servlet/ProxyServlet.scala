package com.twitter.service.cachet.servlet

import _root_.com.twitter.service.cache.client.ForwardRequest
import _root_.javax.servlet._
import client.{JettyHttpClient, ApacheHttpClient}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

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