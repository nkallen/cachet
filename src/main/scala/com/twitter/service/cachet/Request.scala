package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mortbay.io.Buffer
import org.mortbay.jetty.client.HttpClient
import org.mortbay.jetty.client.HttpExchange.ContentExchange
import org.mortbay.jetty.HttpFields.Field

object Request {
  val client = new HttpClient
  client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL)
  client.start()

  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    if (request.isInitial) {
      request.suspend()

      val exchange = new ContentExchange {
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

      exchange.setMethod(request.getMethod)
      exchange.setURL("http://localhost:3000" + request.getRequestURI)
      client.send(exchange)
    }
  }
}