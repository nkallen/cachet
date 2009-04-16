package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream
import java.net.{ConnectException, SocketTimeoutException, URI}
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.scheme.{SchemeRegistry, Scheme}
import org.apache.http.entity.InputStreamEntity
import org.apache.http.params.CoreConnectionPNames
import org.apache.http.HttpVersion
import org.apache.http.impl.client.{DefaultHttpClient, RequestWrapper}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.message.BasicRequestLine
import org.apache.http.params.BasicHttpParams
import org.apache.http.conn.params.ConnManagerParams
import org.apache.http.conn.scheme.PlainSocketFactory

class ApacheHttpClient(timeout: Long, numThreads: Int) extends HttpClient {
  // FIXME: Timeout is not used
  private val params = new BasicHttpParams
  private val log = Logger.get
  ConnManagerParams.setMaxTotalConnections(params, numThreads)
  params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout.toInt)
  params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout.toInt)

  private val schemeRegistry = new SchemeRegistry
  // FIXME: Also support HTTPS
  schemeRegistry.register(
    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80))

  private val connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry)
  private val client = new org.apache.http.impl.client.DefaultHttpClient(connectionManager, params)

  def apply(host: String, port: Int, requestSpecification: RequestSpecification, servletResponse: HttpServletResponse) {
    val request = new ApacheRequest(requestSpecification.method, requestSpecification.uri, requestSpecification.headers, requestSpecification.inputStream)
    val httpHost = new org.apache.http.HttpHost(host, port, requestSpecification.scheme)

    // FIXME: add timings.
    try {
      val response = client.execute(httpHost, request)
      log.info("client params: " + client.getParams())

      for (header <- response.getAllHeaders)
        servletResponse.addHeader(header.getName, header.getValue)

      if (response.getEntity != null) response.getEntity.writeTo(servletResponse.getOutputStream)
      Stats.w3c.log("rs-response-code", response.getStatusLine().getStatusCode())
      if (response.getEntity() != null) {
        Stats.w3c.log("rs-content-length", response.getEntity().getContentLength())
        if (response.getEntity.getContentType() != null) {
          Stats.w3c.log("rs-content-type", response.getEntity().getContentType().getValue())
        }
      }
    } catch {
      case _: SocketTimeoutException => {
        log.warning("backend timed out while reading, returning 504 to client.")
        Stats.w3c.log("sc-response-code", HttpServletResponse.SC_GATEWAY_TIMEOUT)
        servletResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT)
      }
      case _: ConnectException => {
        log.warning("backend timed out while connecting, returning 504 to the client.")
        Stats.w3c.log("sc-response-code", HttpServletResponse.SC_GATEWAY_TIMEOUT)
        servletResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT)
      }
    }
  }

  private class ApacheRequest(method: String, uri: String, headers: Seq[(String, String)], inputStream: InputStream) extends org.apache.http.client.methods.HttpEntityEnclosingRequestBase with org.apache.http.HttpRequest {
    for ((headerName, headerValue) <- headers)
      addHeader(headerName, headerValue)

    override def getMethod = method

    override def getEntity = new InputStreamEntity(inputStream, -1)

    setURI(URI.create(uri))
  }
}
