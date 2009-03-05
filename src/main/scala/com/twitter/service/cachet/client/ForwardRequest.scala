package com.twitter.service.cache.client

import cachet.client.HttpClient
import java.lang.String
import java.util.{ArrayList, List}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import java.util.Collections._

object ForwardRequest {
  val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")
}

class ForwardRequest(httpClient: HttpClient) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    httpClient.newRequest.execute("localhost", 3000, request, response)
  }
}

class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
  override def addHeader(name: String, value: String) = {
    if (!ForwardRequest.hopByHopHeaders.contains(name)) super.addHeader(name, value)
  }
}

class RequestWrapper(request: HttpServletRequest) extends javax.servlet.http.HttpServletRequestWrapper(request) {
  override def getHeaders(name: String) = {
    if (!ForwardRequest.hopByHopHeaders.contains(name))
      super.getHeaders(name)
    else
      enumeration(new ArrayList)
  }
}