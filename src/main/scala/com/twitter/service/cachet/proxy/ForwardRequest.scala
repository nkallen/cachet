package com.twitter.service.cachet.proxy.client

import java.lang.String
import java.util.{ArrayList, List, Vector}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import scala.collection.mutable
import java.util.Collections._

object ForwardRequest {
  // TODO: Find the RFC 2616 Section where these are
  // defined. Content-Length is not a hop-by-hop header, it is an
  // end-to-end header but the Apache HTTP Client does NOT like having
  // it set in advance.
  val hopByHopHeaders = Array("Proxy-Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade", "Content-Length", "Connection")

  // A hashtable of HopByHop headers for quick lookup
  val hopByHopHeadersMap = Map.empty ++ hopByHopHeaders.zip(hopByHopHeaders)
  // Set this to true if you want to disable HTTP Keep-Alives.
  var forceConnectionClose = false
  val CACHET_HEADER_STRING = "cachet.header."
  val CACHET_HEADER_LEN = CACHET_HEADER_STRING.length
}

/**
 * Forwards Requests to the backend.
 */
class ForwardRequest(httpClient: HttpClient, host: String, port: Int) {

  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    // FIXME: add version via configgy.
    response.addHeader("Via", "Cachet/%s".format("0.10"))
    if (ForwardRequest.forceConnectionClose) {
      response.addHeader("Connection", "close")
    }
    val headers = getNewHeaders(request)
    httpClient(host, port, new RequestSpecification(request), new ResponseWrapper(response, headers))
  }

  def getNewHeaders(request: HttpServletRequest): Map[String, String] = {
    val enum = request.getAttributeNames
    val names = new mutable.ListBuffer[String]

    while (enum.hasMoreElements) {
      val name = enum.nextElement.asInstanceOf[String]
      if (name.startsWith(ForwardRequest.CACHET_HEADER_STRING)) {
        names += name
      }
    }

    Map.empty ++ names.map { name => (name.substring(ForwardRequest.CACHET_HEADER_LEN, name.length) ->
                                      request.getAttribute(name).asInstanceOf[String]) }
  }
}

/**
 * @param headers the Headers we want to write over.
 */
class ResponseWrapper(response: HttpServletResponse, headers: Map[String, String]) extends javax.servlet.http.HttpServletResponseWrapper(response) {
  headers.keySet.foreach {key => super.addHeader(key, headers.getOrElse(key, null))}

  def this(response: HttpServletResponse) = this(response, Map.empty)

  // FIXME: add generic Response header rewriting rules.
  override def addHeader(name: String, value: String) = {
    if (!ForwardRequest.hopByHopHeadersMap.contains(name) && !headers.contains(name)) super.addHeader(name, value)
  }
}

/**
 * Removes hopByHop Headers from the incoming request, adds an X-Forwarded-For header, for talking to a backend.
 *
 * Also handles any request rewriting that must occur.
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

  def getRemoteAddr = request.getRemoteAddr

  def headers: Seq[(String, String)] = {
    val headers = new mutable.ListBuffer[(String, String)]
    headers += (X_FORWARDED_FOR, xForwardedFor)
    val headerNames = request.getHeaderNames()
    while (headerNames.hasMoreElements) {
      val headerName = headerNames.nextElement().asInstanceOf[String]
      if (headerName != X_FORWARDED_FOR && !ForwardRequest.hopByHopHeadersMap.contains(headerName)) {
        val hdrs = request.getHeaders(headerName)
        while(hdrs.hasMoreElements) {
          headers += (headerName, hdrs.nextElement().asInstanceOf[String])
        }
      }
    }

    headers
  }

  def _headers: Seq[(String, String)] = {
    // 1. Ensure test coverage
    // 2. Convert this to an Enumerator over ArrayList
    // Put them into a ListBuffer, conver to Seq??
    val headers = (for (headerName <- list(request.getHeaderNames).asInstanceOf[ArrayList[String]] if !ForwardRequest.hopByHopHeaders.contains(headerName) && headerName != "X-Forwarded-For";
          headerValue <- list(request.getHeaders(headerName)).asInstanceOf[ArrayList[String]])
                   yield (headerName, headerValue)) ++ (Seq(("X-Forwarded-For", xForwardedFor)))

    // FIXME: header processing here.
    headers
  }

  private val X_FORWARDED_FOR = "X-Forwarded-For"

  private def xForwardedFor = {
    if (request.getHeader(X_FORWARDED_FOR) == null)
      request.getRemoteAddr
    else
      request.getHeader(X_FORWARDED_FOR) + ", " + request.getRemoteAddr
  }

  override def toString = "Request: ip = %s scheme = %s method = %s uri = %s headers = %s".format(request.getRemoteAddr(), scheme, method, uri, headers)
}
