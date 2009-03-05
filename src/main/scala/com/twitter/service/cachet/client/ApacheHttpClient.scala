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
    def execute(host: String, port: Int, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
      val request = new RawApacheRequest(servletRequest.getMethod, servletRequest.getRequestURI, headers(servletRequest), servletRequest.getInputStream)
      val httpHost = new org.apache.http.HttpHost(host, port, servletRequest.getScheme)

      val response = client.execute(httpHost, request)

      for (header <- response.getAllHeaders)
        servletResponse.addHeader(header.getName, header.getValue)
      response.getEntity.writeTo(servletResponse.getOutputStream())
    }

    private class RawApacheRequest(method: String, uri: String, headers: List[(String, String)], inputStream: InputStream) extends org.apache.http.client.methods.HttpEntityEnclosingRequestBase with org.apache.http.HttpRequest {
      for ((headerName, headerValue) <- headers)
        addHeader(headerName, headerValue)

      override def getMethod = method

      override def getEntity = new InputStreamEntity(inputStream, -1)

      override def getProtocolVersion = null

      override def getRequestLine = new org.apache.http.message.BasicRequestLine(method, uri, null)
    }
  }
}