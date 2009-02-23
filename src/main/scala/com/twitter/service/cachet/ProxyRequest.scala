package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.io.Buffer
import org.mortbay.jetty.client.{HttpExchange, HttpClient}
import org.mortbay.jetty.HttpFields.Field

class ProxyRequest(client: HttpClient, Exchange: (HttpServletRequest, HttpServletResponse) => HttpExchange) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    if (request.isInitial) {
      val exchange = Exchange(request, response)
      exchange.setMethod(request.getMethod)
      exchange.setURL("http://localhost:3000" + request.getRequestURI)
      client.send(exchange)
    }
  }
}