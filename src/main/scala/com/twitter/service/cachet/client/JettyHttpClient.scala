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
    private var exchange = new HttpExchange

    var host = null: String
    var port = 80: Int
    var scheme = null: String
    var method = null: String
    var uri = null: String
    var queryString = null: String
    var inputStream = null: InputStream

    def addHeader(name: String, value: String) {
      exchange.addRequestHeader(name, value)
    }

    def performAndWriteTo(response: HttpServletResponse) {
      exchange.setRequestContentSource(inputStream)
      exchange.setMethod(method)
      exchange.setURL(scheme + "://" + host + ":" + port + uri + (if (queryString != null) "?" + queryString else ""))
      exchange.response = response
      client.send(exchange)
      exchange.waitForDone()
    }

    private class HttpExchange extends org.mortbay.jetty.client.HttpExchange {
      var response = null: HttpServletResponse

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