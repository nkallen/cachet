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
    httpClient.newRequest.execute("192.168.0.101", 80, new RequestWrapper(request), new ResponseWrapper(response))
    response.addHeader("Via", "NProxy")
  }
}

class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
  override def addHeader(name: String, value: String) = {
    if (!ForwardRequest.hopByHopHeaders.contains(name)) super.addHeader(name, value)
  }
}

class RequestWrapper(request: HttpServletRequest) extends javax.servlet.http.HttpServletRequestWrapper(request) {
  override def getHeaders(name: String): java.util.Enumeration[_] = {
    if (ForwardRequest.hopByHopHeaders.contains(name))
      enumeration(new Vector)
    else if (name == "X-Forwarded-For") {
      val v = new Vector[AnyRef]
      v.add(xForwardedFor)
      enumeration(v)
    } else
      super.getHeaders(name)
  }

  private def xForwardedFor = {
    if (request.getHeader("X-Forwarded-For") == null)
      request.getRemoteAddr
    else
      request.getHeader("X-Forwarded-For") + ", " + request.getRemoteAddr
  }
}