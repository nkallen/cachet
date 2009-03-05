package com.twitter.service.cachet.client

import _root_.javax.servlet.http.HttpServletResponse
import org.apache.http.HttpHost
import org.apache.http.impl.client.DefaultHttpClient

trait HttpClient {
  def newRequest: HttpRequest
}