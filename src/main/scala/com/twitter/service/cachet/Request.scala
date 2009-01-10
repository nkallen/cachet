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
    exchange.setMethod("GET")
    exchange.setURL("http://www.example.com/")
    client.send(exchange)
    exchange.waitForDone()
  }
}