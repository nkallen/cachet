package com.twitter.service.cachet.proxy.client

import _root_.com.twitter.service.cache.proxy.client.RequestSpecification
import _root_.javax.servlet.http.HttpServletResponse

trait HttpClient {
  def apply(host: String, port: Int, request: RequestSpecification, response: HttpServletResponse)
}