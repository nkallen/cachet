package com.twitter.service.cachet.client

import _root_.javax.servlet.http.HttpServletRequest

trait HttpRequest {
  def setHost(host: String)

  def setPort(port: Int)

  def setScheme(scheme: String)

  def setMethod(method: String)

  def setUri(uri: String)

  def setQueryString(queryString: String)

  def addHeader(name: String, value: String)

  def writeTo(request: HttpServletRequest)
}