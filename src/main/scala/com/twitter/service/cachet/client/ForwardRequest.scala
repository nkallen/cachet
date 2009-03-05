package com.twitter.service.cache.client

import cachet.client.HttpClient
import java.lang.String
import java.util.List
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._
import java.util.Collections._

class ForwardRequest(httpClient: HttpClient) {
  private val hopByHopHeaders = Array("Proxy-Connection", "Connection", "Keep-Alive", "Transfer-Encoding", "TE", "Trailer", "Proxy-Authorization", "Proxy-Authenticate", "Upgrade")

  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    httpClient.newRequest.execute("localhost", 3000, request, response)
  }

  private class ResponseWrapper(response: HttpServletResponse) extends javax.servlet.http.HttpServletResponseWrapper(response) {
    override def addHeader(name: String, value: String) = {
      if (!hopByHopHeaders.contains(name)) super.addHeader(name, value)
    }
  }

  private class RequestWrapper(request: HttpServletRequest) extends javax.servlet.http.HttpServletRequestWrapper(request) {
    //    override def getHeaderNames = {
    //      for (headerName <- list(request.getHeaderNames).asInstanceOf[List[String]])
    //        1
    //    }
  }
}