package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
//FIXME import java.io.IOException
import java.io.InputStream
import java.net.{SocketException, ConnectException, SocketTimeoutException, URI}
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
  private val params = new BasicHttpParams
  private val log = Logger.get
  ConnManagerParams.setMaxTotalConnections(params, numThreads)
  params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout.toInt)
  params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout.toInt)

  private val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(
    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80))

  /** FIXME: support https
  schemeRegistry.register(
    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443))
    **/
  

  private val connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry)
  private val client = new org.apache.http.impl.client.DefaultHttpClient(connectionManager, params)

  def apply(host: String, port: Int, requestSpecification: RequestSpecification, servletResponse: HttpServletResponse) {
    val log = Logger.get
    Stats.w3c.log("rs-response-method", requestSpecification.method)
    Stats.w3c.log("uri", requestSpecification.uri)
  
    val request = new ApacheRequest(requestSpecification.method, requestSpecification.uri, requestSpecification.headers, requestSpecification.inputStream)
    val httpHost = new org.apache.http.HttpHost(host, port, requestSpecification.scheme)

    try {
      val response = client.execute(httpHost, request)

      for (header <- response.getAllHeaders)
        servletResponse.addHeader(header.getName, header.getValue)

      val entity = response.getEntity()
      val statusLine = response.getStatusLine()
      //FIXME need sc-response-code, also check values of content-length and content-type
      if (entity != null) {
        val ctyp = entity.getContentType() 
        val ty = if (ctyp != null) (ctyp.getName() + ctyp.getValue()) else null
        log.info("content-length=%d, content-type=%s, status-line=%s". format(entity.getContentLength(), ctyp, statusLine))
        entity.writeTo(servletResponse.getOutputStream)
        Stats.w3c.log("rs-content-length", entity.getContentLength())
        //FIXME: Stats.w3c.log("rs-content-type", ctyp)
      }
      Stats.w3c.log("rs-response-code", statusLine.getStatusCode())
    } catch {
      /***
      // FIXME: check this for all exceptions http://java.sun.com/j2se/1.4.2/docs/api/index.html
      case _: SocketTimeoutException => {
        log.warning("SocketTimeoutException: backend timed out while reading, returning 504 to client.")
        Stats.w3c.log("sc-response-code", HttpServletResponse.SC_GATEWAY_TIMEOUT)
        servletResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT)
      }
      case _: ConnectException => {
        log.warning("ConnectException: backend timed out while connecting, returning 504 to the client.")
        Stats.w3c.log("sc-response-code", HttpServletResponse.SC_GATEWAY_TIMEOUT)
        servletResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT)
      }
      ***/
      case e => {
        log.warning("%s: backend timed out while reading (message='%s'), returning 504 to client.".format(e.toString, e.getMessage()))
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
