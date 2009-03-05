package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream
import org.mortbay.io.Buffer

class JettyHttpClient extends HttpClient {
  val client = new org.mortbay.jetty.client.HttpClient
  client.start()

  def newRequest: HttpRequest = {
    new JettyHttpRequest
  }

  private class JettyHttpRequest extends HttpRequest {
    def execute(host: String, port: Int, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
      var exchange = new HttpExchange(servletResponse)
      exchange.setRequestContentSource(servletRequest.getInputStream)
      exchange.setMethod(servletRequest.getMethod)
      exchange.setURL(servletRequest.getScheme + "://" + host + ":" + port + servletRequest.getRequestURI + (if (servletRequest.getQueryString != null) "?" + servletRequest.getQueryString else ""))
      for ((headerName, headerValue) <- headers(servletRequest))
        exchange.addRequestHeader(headerName, headerValue)
      client.send(exchange)
      exchange.waitForDone()
    }

    private class HttpExchange(response: HttpServletResponse) extends org.mortbay.jetty.client.HttpExchange {
      override def onResponseHeader(name: Buffer, value: Buffer) {
        response.addHeader(name.toString, value.toString)
      }

      override def onResponseContent(content: Buffer) {
        content.writeTo(response.getOutputStream)
      }

      override def onResponseStatus(version: Buffer, status: Int, reason: Buffer) {
        response.setStatus(status)
      }
    }
  }
}