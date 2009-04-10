package com.twitter.service.cachet.proxy.client

import javax.servlet.http.HttpServletResponse

trait HttpClient {
  def apply(host: String, port: Int, request: RequestSpecification, response: HttpServletResponse)
}
