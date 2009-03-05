package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream
import java.net.URI
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.InputStreamEntity
import org.apache.http.HttpVersion
import org.apache.http.impl.client.RequestWrapper
import org.apache.http.message.BasicRequestLine

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

      if (response.getEntity != null) response.getEntity.writeTo(servletResponse.getOutputStream)
    }

    private class RawApacheRequest(method: String, uri: String, headers: Seq[(String, String)], inputStream: InputStream) extends org.apache.http.client.methods.HttpEntityEnclosingRequestBase with org.apache.http.HttpRequest {
      for ((headerName, headerValue) <- headers)
        addHeader(headerName, headerValue)

      override def getMethod = method

      override def getEntity = new InputStreamEntity(inputStream, -1)

      setURI(URI.create(uri))
    }
  }
}