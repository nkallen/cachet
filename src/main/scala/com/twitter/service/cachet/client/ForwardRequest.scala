package com.twitter.service.cache.client

import java.util.List
import java.util.Collections._
import javax.servlet.http.HttpServletRequest
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.message.BasicHttpRequest
import javax.servlet.http.HttpServletResponse
import org.apache.http.{HttpRequest, HttpHost}
import scala.collection.jcl.Conversions._

class ForwardRequest(client: HttpClient, ClientRequest: (String, String) => HttpRequest) {
  def apply(request: HttpServletRequest, response: HttpServletResponse) {
    val (forwardHost, forwardRequest) = constructForwardRequest(request)
    val forwardResponse = client.execute(forwardHost, forwardRequest)
    ()
  }

  private def constructForwardRequest(request: HttpServletRequest) = {
    val queryString = if (request.getQueryString != null) "?" + request.getQueryString else ""

    val forwardRequest = ClientRequest(request.getMethod, request.getRequestURI + queryString)
    for (headerName <- list(request.getHeaderNames).asInstanceOf[List[String]];
         headerValue <- list(request.getHeaders(headerName)).asInstanceOf[List[String]])
      forwardRequest.addHeader(headerName, headerValue)

    (new HttpHost("localhost", 3000, request.getScheme), forwardRequest)
  }

  //  private def copyResponse(forwardResponse, response) {
  //    for (headerName <- forwardResponse.getHeaderNameSet;
  //      headerValue <- forwardResponse.getHeaderList(headerName))
  //        response.addHeader(headerName, headerValue)
  //
  //    forwardResponse.getBlockingBody.writeTo(response.getOutputStream)
  //  }
}