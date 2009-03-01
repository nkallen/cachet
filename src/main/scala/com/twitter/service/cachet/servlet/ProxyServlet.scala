package com.twitter.service.cachet.servlet

import _root_.com.twitter.service.cache.client.ClientRequest
import _root_.javax.servlet._
import client.{CopyExchange, ClientRequest}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.mortbay.jetty.client.HttpClient

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var clientRequest = null: ClientRequest

  override def init(c: ServletConfig) {
    config = c
    val client = new HttpClient
    client.start()
    clientRequest = new ClientRequest(client, (request, response) => new CopyExchange(request, response))
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