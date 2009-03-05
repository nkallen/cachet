package com.twitter.service.cache.client

import cachet.client.HttpClient
import java.lang.String
import java.util.List
import java.util.Collections._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._

class ForwardRequest(httpClient: HttpClient) {
  private val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")

  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    val forwardRequest = httpClient.newRequest
    forwardRequest.host = "localhost"
    forwardRequest.port = 3000
    forwardRequest.scheme = request.getScheme
    forwardRequest.method = request.getMethod
    forwardRequest.uri = request.getRequestURI
    forwardRequest.queryString = request.getQueryString
    forwardRequest.inputStream = request.getInputStream
    for (headerName <- list(request.getHeaderNames).asInstanceOf[List[String]];
         headerValue <- list(request.getHeaders(headerName)).asInstanceOf[List[String]])
      if (!hopByHopHeaders.contains(headerName))
        forwardRequest.addHeader(headerName, headerValue)

    forwardRequest.performAndWriteTo(response)
  }

  private class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
    override def addHeader(name: String, value: String) = {
      if (!hopByHopHeaders.contains(name)) super.addHeader(name, value)
    }
  }
}