package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.jetty.client.HttpClient
import org.mortbay.jetty.client.HttpExchange.ContentExchange

object Request {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    val client = new HttpClient
    client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL)
    client.start()

    val exchange = new ContentExchange
    exchange.setMethod(request.getMethod)
    exchange.setURL("http://localhost:3000" + request.getRequestURI)
    client.send(exchange)
    exchange.waitForDone()
    response.setStatus(exchange.getStatus)
    response.getOutputStream.print(exchange.getResponseContent)
  }
}