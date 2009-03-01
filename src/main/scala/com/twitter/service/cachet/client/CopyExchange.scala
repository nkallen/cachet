package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.io.Buffer
import org.mortbay.jetty.client.{HttpClient, HttpExchange}

class CopyExchange(request: HttpServletRequest, response: HttpServletResponse) extends HttpExchange {
  request.suspend()

  override def onResponseHeader(name: Buffer, value: Buffer) {
    response.setHeader(name.toString, value.toString)
  }

  override def onResponseContent(content: Buffer) {
    content.writeTo(response.getOutputStream)
  }

  override def onResponseStatus(version: Buffer, status: Int, reason: Buffer) {
    response.setStatus(status)
  }

  override def onResponseComplete {
    request.resume()
  }
}