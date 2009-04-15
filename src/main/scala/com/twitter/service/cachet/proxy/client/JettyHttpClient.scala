package com.twitter.service.cachet.proxy.client

import net.lag.logging.Logger
import org.mortbay.io.Buffer
import org.mortbay.thread.QueuedThreadPool
import org.mortbay.jetty.client.{HttpClient => MortbayHttpClient}
import org.mortbay.jetty.client.Address
import org.mortbay.jetty.HttpSchemes
import scala.collection.mutable
import java.lang.Throwable
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream

class JettyHttpClient(timeout: Long, numThreads: Int) extends HttpClient {
  val client = new MortbayHttpClient
  client.setTimeout(timeout)
  client.setThreadPool(ThreadPool(numThreads))
  client.start()

  def apply(host: String, port: Int, requestSpecification: RequestSpecification, servletResponse: HttpServletResponse) {
    var exchange = new HttpExchange(servletResponse)
    exchange.setRequestContentSource(requestSpecification.inputStream)

    Stats.w3c.log("rs-response-method", requestSpecification.method)
    exchange.setMethod(requestSpecification.method)

    exchange.setAddress(new Address(host, port))
    exchange.setScheme(
      if (HttpSchemes.HTTPS.equals(requestSpecification.scheme))
        HttpSchemes.HTTPS_BUFFER
      else
        HttpSchemes.HTTP_BUFFER
      )
    Stats.w3c.log("uri", requestSpecification.uri)
    exchange.setURI(requestSpecification.uri)
    for ((headerName, headerValue) <- requestSpecification.headers)
      exchange.addRequestHeader(headerName, headerValue)
    try {
      client.send(exchange)
      exchange.waitForDone()
    } finally {
      Stats.w3c.log("rs-response-code", exchange.headerMap.getOrElse("Code", null))
      Stats.w3c.log("rs-content-type", exchange.headerMap.getOrElse("Content-Type", null))
      Stats.w3c.log("rs-content-length", exchange.headerMap.getOrElse("Content-Length", null))
      Stats.w3c.log("sc-response-code", exchange.headerMap.getOrElse("sc-response-code", null))
    }
  }

  private class HttpExchange[T](response: HttpServletResponse) extends org.mortbay.jetty.client.HttpExchange {
    // A placeholder so we can access the response headers from the main thread.
    var headerMap: mutable.Map[String, String] = new mutable.HashMap[String, String]()
    val log = Logger.get
    override def onResponseHeader(name: Buffer, value: Buffer) {
      headerMap + (name.toString -> value.toString)
      response.addHeader(name.toString, value.toString)
    }

    override def onResponseContent(content: Buffer) {
      content.writeTo(response.getOutputStream)
    }

    override def onResponseStatus(version: Buffer, status: Int, reason: Buffer) {
      headerMap + ("Code" -> status.toString)
      response.setStatus(status)
    }

    override def onExpire = {
      log.warning("onExpire called")
      response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT )
    }

    override def onException(ex: Throwable) = {
      headerMap + ("sc-response-code" -> HttpServletResponse.SC_GATEWAY_TIMEOUT.toString)
      log.warning("Request to backend failed: %s caused by %s ".format(ex, ex.getCause()))
      response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT )
    }

    override def onRetry() {
      log.warning("Retrying request")
      super.onRetry()
    }


    override def onConnectionFailed(ex: Throwable) = onException(ex)
  }
}
