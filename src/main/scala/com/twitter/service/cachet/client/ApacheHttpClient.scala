package com.twitter.service.cachet.client

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.http.HttpHost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicHttpRequest

class ApacheHttpClient extends HttpClient {
  private val client = new DefaultHttpClient
  private val request = new BasicHttpRequest

  var host = null: String
  var port = null: Int
  var scheme = null: String
  var method = null: String
  var uri = null: String
  var queryString = null: String

  def addHeader(name: String, value: String) {
    request.addHeader(name, value)
  }

  def performRequestAndWriteTo(response: HttpServletResponse) {
    val host = new HttpHost(host, port, scheme)
    val response = client.execute(host, request)
    for (headerName <- response.getHeaderNameSet;
         headerValue <- response.getHeaderList(headerName))
      response.addHeader(headerName, headerValue)
  }
}