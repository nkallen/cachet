package com.twitter.service.cachet.servlet

import _root_.com.twitter.service.cache.client.ForwardRequest
import _root_.javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var clientRequest = null: ForwardRequest

  override def init(c: ServletConfig) {
    config = c
    val client = new DefaultHttpClient
    clientRequest = new ForwardRequest(client)
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    clientRequest(request, response)
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    clientRequest(request, response)
  }

  override def doPut(request: HttpServletRequest, response: HttpServletResponse) {
    clientRequest(request, response)
  }

  override def doDelete(request: HttpServletRequest, response: HttpServletResponse) {
    clientRequest(request, response)
  }
}