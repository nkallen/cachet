package com.twitter.service.cachet.client

import _root_.com.twitter.service.cache.client.RequestSpecification
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.util.ArrayList
import scala.collection.jcl.Conversions._
import java.util.Collections._

trait HttpRequest {
  def execute(host: String, port: Int, request: RequestSpecification, response: HttpServletResponse)

  protected def uriWithQueryString(requestSpecification: RequestSpecification) = {
    val queryString = if (requestSpecification.queryString != null)
      "?" + requestSpecification.queryString
    else ""
    requestSpecification.uri + queryString
  }
}