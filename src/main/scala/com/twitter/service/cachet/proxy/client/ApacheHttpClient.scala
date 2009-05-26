package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import java.io.InputStream
import java.net.{InetAddress, Socket, SocketException, ConnectException, SocketTimeoutException, URI}
import javax.net.ssl.SSLSocket
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.http.{HttpResponse, HttpVersion}
import org.apache.http.client.RedirectHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.params._
import org.apache.http.conn.scheme.{PlainSocketFactory, LayeredSocketFactory, SchemeRegistry, Scheme}
import org.apache.http.conn.ssl.{AllowAllHostnameVerifier, SSLSocketFactory}
import org.apache.http.entity.{BufferedHttpEntity, InputStreamEntity}
import org.apache.http.message.BasicRequestLine
import org.apache.http.impl.client.{DefaultHttpClient, RequestWrapper}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.{BasicHttpParams, CoreConnectionPNames, CoreProtocolPNames, HttpParams, HttpProtocolParams}
import org.apache.http.protocol.HttpContext


class ApacheHttpClient(timeout: Long, numThreads: Int, port: Int, sslPort: Int, soBufferSize: Int) extends HttpClient {
  private val log = Logger.get
  private val params = new BasicHttpParams

  // The HTTP spec only allows 2 concurrent connections per host by default, this allows us to
  // make Integer.MAX_VALUE concurrent connections per host.
  val twitterRouter = new ConnPerRouteBean(Integer.MAX_VALUE)

  params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, twitterRouter)
  params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout.toInt)
  params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
  params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout.toInt)
  params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false)
  params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
  params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, soBufferSize)
  //HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)

  val sslSocketFactory = SSLSocketFactory.getSocketFactory()
  sslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier)

  private val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port))
  schemeRegistry.register(new Scheme("https", sslSocketFactory, sslPort))

  private val connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry)
  private val client = new org.apache.http.impl.client.DefaultHttpClient(connectionManager, params)
  // We do not wish to handle redirects automatically, pass them back to the user.
  client.setRedirectHandler(new RedirectHandler() {
    override def isRedirectRequested(response: HttpResponse,  context: HttpContext): Boolean = false
    override def getLocationURI(response: HttpResponse, context: HttpContext): URI = null
  })

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

      client.getCookieStore().clear()

      val statusLine = response.getStatusLine()
      val statusCode = statusLine.getStatusCode
      servletResponse.setStatus(statusCode)
      Stats.w3c.log("rs-response-code", statusCode)

      val entity = response.getEntity()
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
        Stats.w3c.log("rs-content-length", entity.getContentLength())
        Stats.w3c.log("rs-content-type", contentType)
        entity.writeTo(servletResponse.getOutputStream)
      }
    } catch {
      case e => {
        log.error(e, "%s: backend timed out while connection (message='%s', cause='%s'), returning 504 to client.".format(e.toString, e.getMessage(), e.getCause()))
        Stats.w3c.log("sc-response-code", HttpServletResponse.SC_GATEWAY_TIMEOUT)
        servletResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT)
      }
    }
  }

  private class ApacheRequest(method: String, uri: String, headers: Seq[(String, String)], inputStream: InputStream) extends org.apache.http.client.methods.HttpEntityEnclosingRequestBase with org.apache.http.HttpRequest {
    for ((headerName, headerValue) <- headers)
      addHeader(headerName, headerValue)

    override def getMethod = method

    /**
     * The Entity is the Request Body.
     */
    override def getEntity = new BufferedHttpEntity(new InputStreamEntity(inputStream, -1))

    setURI(URI.create(uri))
  }
}
