package com.twitter.service.cache.proxy.client

import cachet.proxy.client.HttpClient
import java.lang.String
import java.util.{ArrayList, List, Vector}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import java.util.Collections._

object ForwardRequest {
  // TODO: Find the RFC 2616 Section where these are defined.
  val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")
}

/**
 * Forwards Requests to the backend.
 */
class ForwardRequest(httpClient: HttpClient, host: String, port: Int) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    httpClient(host, port, new RequestSpecification(request), new ResponseWrapper(response))
    // FIXME: add version via configgy.
    response.addHeader("Via", "Cachet/%s".format("0.10")
  }
}

class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
  override def addHeader(name: String, value: String) = {
    if (!ForwardRequest.hopByHopHeaders.contains(name)) super.addHeader(name, value)
  }
}

/**
 * Removes hopByHop Headers from the incoming request, adds an X-Forwarded-For header, for talking to a backend.
 */
class RequestSpecification(request: HttpServletRequest) {
  def scheme = request.getScheme

  /**
   * Returns resource and query string, not scheme or host.
   */
  def uri = {
    val queryString = if (request.getQueryString != null)
      "?" + request.getQueryString
    else ""
    request.getRequestURI + queryString
  }

  def inputStream = request.getInputStream

  def queryString = request.getQueryString

  def method = request.getMethod

  def headers: Seq[(String, String)] = {
    (for (headerName <- list(request.getHeaderNames).asInstanceOf[ArrayList[String]] if !ForwardRequest.hopByHopHeaders.contains(headerName) && headerName != "X-Forwarded-For";
          headerValue <- list(request.getHeaders(headerName)).asInstanceOf[ArrayList[String]])
    yield (headerName, headerValue)) ++ (Seq(("X-Forwarded-For", xForwardedFor)))
  }

  private def xForwardedFor = {
    if (request.getHeader("X-Forwarded-For") == null)
      request.getRemoteAddr
    else
      request.getHeader("X-Forwarded-For") + ", " + request.getRemoteAddr
  }
}
