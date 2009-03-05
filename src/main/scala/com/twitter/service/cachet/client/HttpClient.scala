package com.twitter.service.cachet.client

import _root_.javax.servlet.http.HttpServletResponse
import org.apache.http.HttpHost
import org.apache.http.impl.client.DefaultHttpClient

trait HttpClient {
  def host = (host: String)

  def port = (port: Int)

  def scheme = (scheme: String)

  def method = (method: String)

  def uri = (uri: String)

  def queryString = (queryString: String)

  def addHeader(name: String, value: String)

  def performRequestAndWriteTo(response: HttpServletResponse)
}