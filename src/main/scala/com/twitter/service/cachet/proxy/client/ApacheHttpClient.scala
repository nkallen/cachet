package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
//FIXME import java.io.IOException
import java.io.InputStream
import java.net.{SocketException, ConnectException, SocketTimeoutException, URI}
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.scheme.{SchemeRegistry, Scheme}
import org.apache.http.entity.InputStreamEntity
import org.apache.http.conn.params._
import org.apache.http.params._
import org.apache.http.HttpVersion
import org.apache.http.impl.client.{DefaultHttpClient, RequestWrapper}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.message.BasicRequestLine
import org.apache.http.params.BasicHttpParams
import org.apache.http.conn.params.ConnManagerParams
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory


class ApacheHttpClient(timeout: Long, numThreads: Int) extends HttpClient {
  private val log = Logger.get
  private val params = new BasicHttpParams
  // The HTTP spec only allows 2 concurrent connections per host by default, this allows us to
  // make Integer.MAX_VALUE concurrent connections per host.
  val twitterRouter = new ConnPerRouteBean(Integer.MAX_VALUE)

  params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, twitterRouter)
  params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout.toInt)
  params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
  params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout.toInt)

  private val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(
    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
  schemeRegistry.register(
    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443))

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
      if (entity != null) {
        val contentType = if (entity.getContentType() != null) {
          // Returns the Content-Type minus any parameters (rfc 2045 S5)
          val cType = entity.getContentType.getValue()
          val index = cType.indexOf(";")
          if (index > 0) {
            cType.substring(0, index)
          } else {
            cType
          }
        } else {
          null
        }

        Stats.w3c.log("rs-content-type", contentType)

        entity.writeTo(servletResponse.getOutputStream)
        Stats.w3c.log("rs-content-length", entity.getContentLength())
      }
      Stats.w3c.log("rs-response-code", statusLine.getStatusCode())
    } catch {
      case e => {
        log.warning("%s: backend timed out while connection (message='%s', cause='%s'), returning 504 to client.".format(e.toString, e.getMessage(), e.getCause()))
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
