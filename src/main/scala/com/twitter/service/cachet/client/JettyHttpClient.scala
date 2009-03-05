package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.io.Buffer
import org.mortbay.jetty.client.HttpExchange

class JettyHttpClient extends HttpClient {
  val client = new org.mortbay.jetty.client.HttpClient
  client.start()

  def performRequestAndWriteTo(response: HttpServletResponse) {
    client.send(new CopyExchange(response))
  }
}

class CopyExchange(response: HttpServletResponse) extends HttpExchange {
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