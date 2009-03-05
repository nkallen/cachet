package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream
import org.apache.http.entity.InputStreamEntity

class ApacheHttpClient extends HttpClient {
  private val client = new org.apache.http.impl.client.DefaultHttpClient

  def newRequest: HttpRequest = {
    new ApacheHttpRequest
  }

  private class ApacheHttpRequest extends HttpRequest {
    private val request = new RawApacheRequest

    var host = null: String
    var port = 80: Int
    var scheme = null: String
    var uri = null: String
    var queryString = null: String
    var method = null: String
    var inputStream = null: InputStream

    def addHeader(name: String, value: String) {
      request.addHeader(name, value)
    }

    def performAndWriteTo(servletResponse: HttpServletResponse) {
      val httpHost = new org.apache.http.HttpHost(host, port, scheme)
      val response = client.execute(httpHost, request)

      for (header <- response.getAllHeaders)
        servletResponse.addHeader(header.getName, header.getValue)
      response.getEntity.writeTo(servletResponse.getOutputStream())
    }

    private class RawApacheRequest extends org.apache.http.client.methods.HttpEntityEnclosingRequestBase with org.apache.http.HttpRequest {
      override def getMethod = method

      override def getEntity = new InputStreamEntity(inputStream, -1)

      override def getProtocolVersion = null

      override def getRequestLine = new org.apache.http.message.BasicRequestLine(method, uri, null)
    }
  }
}