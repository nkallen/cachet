package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.ServletConfig
import org.mortbay.jetty.client.HttpClient

class ProxyServlet extends HttpServlet {
  var config: ServletConfig = null
  var proxyRequest: ProxyRequest = null

  override def init(c: ServletConfig) {
    config = c
    val client = new HttpClient
    client.start()
    proxyRequest = new ProxyRequest(client, (request, response) => new CopyExchange(request, response))
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    proxyRequest(request, response)
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    proxyRequest(request, response)
  }

  override def doPut(request: HttpServletRequest, response: HttpServletResponse) {
    proxyRequest(request, response)
  }

  override def doDelete(request: HttpServletRequest, response: HttpServletResponse) {
    proxyRequest(request, response)
  }
}