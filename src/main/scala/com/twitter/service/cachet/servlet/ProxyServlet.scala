package com.twitter.service.cachet.servlet

import _root_.com.twitter.service.cache.client.ForwardRequest
import _root_.javax.servlet._
import client.ApacheHttpClient
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var forwardRequest = null: ForwardRequest

  override def init(c: ServletConfig) {
    config = c
    val client = new ApacheHttpClient
    forwardRequest = new ForwardRequest(client)
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    forwardRequest(request, response)
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    forwardRequest(request, response)
  }

  override def doPut(request: HttpServletRequest, response: HttpServletResponse) {
    forwardRequest(request, response)
  }

  override def doDelete(request: HttpServletRequest, response: HttpServletResponse) {
    forwardRequest(request, response)
  }
}