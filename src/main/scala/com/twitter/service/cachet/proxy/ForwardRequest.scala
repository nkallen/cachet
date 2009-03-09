package com.twitter.service.cache.proxy.client

import cachet.proxy.client.HttpClient
import java.lang.String
import java.util.{ArrayList, List, Vector}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import java.util.Collections._

object ForwardRequest {
  val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")
}

class ForwardRequest(httpClient: HttpClient, host: String, port: Int) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    httpClient(host, port, new RequestSpecification(request), new ResponseWrapper(response))
    response.addHeader("Via", "NProxy")
  }
}

class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
  override def addHeader(name: String, value: String) = {
    if (!ForwardRequest.hopByHopHeaders.contains(name)) super.addHeader(name, value)
  }
}

class RequestSpecification(request: HttpServletRequest) {
  def scheme = request.getScheme

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