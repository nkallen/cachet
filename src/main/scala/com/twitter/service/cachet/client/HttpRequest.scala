package com.twitter.service.cachet.client

import _root_.javax.servlet.http.HttpServletResponse

trait HttpRequest {
  var host: String
  var port: Int
  var scheme: String
  var method: String
  var uri: String
  var queryString: String

  def addHeader(name: String, value: String)

  def performAndWriteTo(response: HttpServletResponse)
}