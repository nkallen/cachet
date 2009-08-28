package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import java.io.{InputStream, IOException}
import java.net.{InetAddress, Socket, SocketException, ConnectException, SocketTimeoutException, URI, URISyntaxException}
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
import org.mortbay.jetty.EofException

class ApacheHttpClient(timeout: Long, numThreads: Int, port: Int, sslPort: Option[Int],
  soBufferSize: Int, errorStrings: Map[Int, String]) extends HttpClient {

  private val log = Logger.get
  private val params = new BasicHttpParams

  // The HTTP spec only allows 2 concurrent connections per host by default, this allows us to
  // make Integer.MAX_VALUE concurrent connections per host.
  // FIXME: Do we want to read this by Config?
  val twitterRouter = new ConnPerRouteBean(Integer.MAX_VALUE)

  params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, twitterRouter)
  // FIXME: Do we want this to be read by Config?
  params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, Integer.MAX_VALUE)
  params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout.toInt)
  params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
  params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout.toInt)
  params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false)
  params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
  params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, soBufferSize)
  //params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH)
  //HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)

  private val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port))

  sslPort map { sp =>
    val sslSocketFactory = SSLSocketFactory.getSocketFactory()
    sslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier)
    schemeRegistry.register(new Scheme("https", sslSocketFactory, sp))
  }

  private val connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry)

  // FIXME: Was hoping this would work to remove cookie processing completely, but it did not
  // Create a basic context that is different from the one used in DefaultHttpClient because
  // it does not do anything to the cookies
  // private val context = new org.apache.http.protocol.BasicHttpContext()
  // context.setAttribute(org.apache.http.client.protocol.ClientContext.SCHEME_REGISTRY, schemeRegistry)
  // context.removeAttribute(org.apache.http.client.protocol.ClientContext.COOKIE_STORE)

  private val client = new org.apache.http.impl.client.DefaultHttpClient(connectionManager, params)
  // We do not wish to handle redirects automatically, pass them back to the user.
  client.setRedirectHandler(new RedirectHandler() {
    override def isRedirectRequested(response: HttpResponse,  context: HttpContext): Boolean = false
    override def getLocationURI(response: HttpResponse, context: HttpContext): URI = null
  })
  // FIXME: Do this per-class removal!
  // client.removeRequestInterceptorByClass(org.apache.http.client.protocol.RequestAddCookies.class)
  // client.removeResponseInterceptorByClass(org.apache.http.client.protocol.ResponseProcessCookies.class)
  client.clearResponseInterceptors()

  private val defaultErrorString = ""

  def apply(host: String, port: Int, requestSpecification: RequestSpecification, servletResponse: HttpServletResponse) {
    val log = Logger.get

    //FIXME: remove this or move it somewhere else: log.ifDebug{ "Threadpool queueSize = %d , idleThreads = %d, threads = %d".format(ThreadPool.getQueueSize(), ThreadPool.getIdleThreads(), ThreadPool.getThreads()) }
    Stats.w3c.log("rs-response-method", requestSpecification.method)
    Stats.w3c.log("uri", requestSpecification.uri)
    var statusCode = 0
    var response: org.apache.http.HttpResponse = null
    try {
      log.ifDebug(requestSpecification.toString)
      val request = new ApacheRequest(requestSpecification.method, requestSpecification.uri, requestSpecification.headers, requestSpecification.inputStream)
      val httpHost = new org.apache.http.HttpHost(host, port, requestSpecification.scheme)
      response = client.execute(httpHost, request)

      for (header <- response.getAllHeaders)
        servletResponse.addHeader(header.getName, header.getValue)

      client.getCookieStore().clear()

      val statusLine = response.getStatusLine()
      statusCode = statusLine.getStatusCode
      servletResponse.setStatus(statusCode)

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
        try {
          entity.writeTo(servletResponse.getOutputStream)
          Stats.clientResponseSent()
        } catch {
          case e: EofException => {
            Stats.w3c.log("rs-went-away", 1) // we ignore, it means the client went away.
            Stats.clientLeftEarly()
          }
        } finally {
          log.ifDebug {
            "Response: statusCode = %s".format(statusCode) +
              " contentType = %s, contentLength = %s, headers = %s" .format(contentType, entity.getContentLength(), response.getAllHeaders().toList.toString)
          }
          entity.consumeContent() // ensure connection release
        }
      }
    } catch {
      case u: URISyntaxException => {
        statusCode = HttpServletResponse.SC_BAD_REQUEST
        log.error(u, "%s: URL %s has incorrect syntax (message='%s', cause='%s'), returning 400 to client.".format(u.toString, requestSpecification.uri, u.getMessage(), u.getCause()))
      }
      case e: IOException => {
        statusCode = HttpServletResponse.SC_GATEWAY_TIMEOUT
        // setServletResponseErrorDoc(statusCode, servletResponse)
        log.error(e, "%s: backend timed out while connection (message='%s', cause='%s'), returning 504 to client.".format(e.toString, e.getMessage(), e.getCause()))
      }
      case e => {
        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        // setServletResponseErrorDoc(statusCode, servletResponse)
        log.error(e, "%s: Exception (message='%s', cause='%s'), returning 500 to client.".format(e.toString, e.getMessage(), e.getCause()))
      }
    }
    if (response == null) {
      // error condition
      servletResponse.setStatus(statusCode)
      if (requestSpecification.method != "HEAD") {
        servletResponse.getOutputStream.print(errorStrings.getOrElse(statusCode, defaultErrorString))
      }
      log.debug("Response: statusCode = %s contentType = null, contentLength = null, headers = null".format(statusCode))
    }
    Stats.w3c.log("sc-response-code", statusCode)
    if (statusCode >= 200 && statusCode <= 299) {
      Stats.returned2xx()
    } else if (statusCode >= 300 && statusCode <= 399) {
      Stats.returned3xx()
    } else if (statusCode >= 400 && statusCode <= 499) {
      Stats.returned4xx()
    } else if (statusCode >= 500 && statusCode <= 599) {
      Stats.returned5xx()
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
