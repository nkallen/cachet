package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.io.Buffer

class JettyHttpClient extends HttpClient {
  val request = new HttpRequest
  val client = new org.mortbay.jetty.client.HttpClient
  client.start()

  //  request.setRequestContentSource(request.getInputStream)

  var host = null: String
  var port = 80: Int
  var scheme = null: String
  var method = null: String
  var uri = null: String
  var queryString = null: String

  def addHeader(name: String, value: String) {
    request.addRequestHeader(name, value)
  }

  def performRequestAndWriteTo(response: HttpServletResponse) {
    request.setMethod(method)
    request.setURL(scheme + "://" + host + ":" + port + uri + (if (queryString != null) "?" + queryString else ""))
    request.response = response
    client.send(request)
    request.waitForDone()
  }

  class HttpRequest extends org.mortbay.jetty.client.HttpExchange {
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