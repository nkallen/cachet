package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.util.ArrayList
import scala.collection.jcl.Conversions._
import java.util.Collections._

trait HttpRequest {
  def execute(host: String, port: Int, request: HttpServletRequest, response: HttpServletResponse)

  protected def headers(request: HttpServletRequest) = {
    for (headerName <- list(request.getHeaderNames).asInstanceOf[ArrayList[String]];
         headerValue <- list(request.getHeaders(headerName)).asInstanceOf[ArrayList[String]])
    yield (headerName, headerValue)
  }
}