package com.twitter.service.cache.client

import cachet.client.HttpClient
import java.util.List
import java.util.Collections._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.jcl.Conversions._

class ForwardRequest(httpClient: HttpClient) {
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
      forwardRequest.addHeader(headerName, headerValue)

    forwardRequest.performAndWriteTo(response)
  }
}