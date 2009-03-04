package com.twitter.service.cache.client

import javax.servlet.http.HttpServletRequest
import org.mortbay.jetty.client.{HttpClient, HttpExchange}
import javax.servlet.http.HttpServletResponse

class ClientRequest(client: HttpClient, Exchange: (HttpServletRequest, HttpServletResponse) => HttpExchange) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    val exchange = Exchange(request, response)
    val headers = request.getHeaderNames
    while (headers.hasMoreElements) {
      var headerName = headers.nextElement.asInstanceOf[String]
      exchange.setRequestHeader(headerName, request.getHeader(headerName))
    }
    exchange.setMethod(request.getMethod)
    exchange.setURL("http://localhost:3000" + request.getRequestURI)
    exchange.setRequestContentSource(request.getInputStream)

    client.send(exchange)
    exchange.waitForDone()
  }
}