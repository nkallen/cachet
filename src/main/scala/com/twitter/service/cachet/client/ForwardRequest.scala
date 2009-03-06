package com.twitter.service.cache.client

import cachet.client.HttpClient
import java.lang.String
import java.util.{ArrayList, List, Vector}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import java.util.Collections._

object ForwardRequest {
  val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")
}

class ForwardRequest(httpClient: HttpClient) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    httpClient.newRequest.execute("192.168.0.101", 80, new RequestSpecification(request), new ResponseWrapper(response))
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

  def uri = request.getRequestURI

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