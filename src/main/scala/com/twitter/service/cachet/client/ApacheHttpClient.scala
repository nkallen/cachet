package com.twitter.service.cachet.client

import _root_.com.twitter.service.cache.client.RequestSpecification
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream
import java.net.URI
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.scheme.{SchemeRegistry, Scheme}
import org.apache.http.entity.InputStreamEntity
import org.apache.http.HttpVersion
import org.apache.http.impl.client.{DefaultHttpClient, RequestWrapper}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.message.BasicRequestLine
import org.apache.http.params.BasicHttpParams
import org.apache.http.conn.params.ConnManagerParams
import org.apache.http.conn.scheme.PlainSocketFactory

class ApacheHttpClient extends HttpClient {
  private val params = new BasicHttpParams
  ConnManagerParams.setMaxTotalConnections(params, 100)

  private val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(
    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80))

  private val connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry)
  private val client = new org.apache.http.impl.client.DefaultHttpClient(connectionManager, params)

  def newRequest: HttpRequest = {
    new ApacheHttpRequest
  }

  private class ApacheHttpRequest extends HttpRequest {
    def execute(host: String, port: Int, requestSpecification: RequestSpecification, servletResponse: HttpServletResponse) {
      val request = new RawApacheRequest(requestSpecification.method, requestSpecification.uri, requestSpecification.headers, requestSpecification.inputStream)
      val httpHost = new org.apache.http.HttpHost(host, port, requestSpecification.scheme)

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