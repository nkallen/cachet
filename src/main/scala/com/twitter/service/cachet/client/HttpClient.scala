package com.twitter.service.cachet.client

import _root_.com.twitter.service.cache.client.RequestSpecification
import _root_.javax.servlet.http.HttpServletResponse

trait HttpClient {
  def newRequest: HttpRequest
}

trait HttpRequest {
  def execute(host: String, port: Int, request: RequestSpecification, response: HttpServletResponse)
}