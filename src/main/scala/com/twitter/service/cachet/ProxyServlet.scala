package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.ServletConfig

class ProxyServlet extends HttpServlet {
  var config: ServletConfig = null

  override def init(c: ServletConfig) {
    config = c
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    Request(request, response)
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    Request(request, response)
  }

  override def doPut(request: HttpServletRequest, response: HttpServletResponse) {
    Request(request, response)
  }

  override def doDelete(request: HttpServletRequest, response: HttpServletResponse) {
    Request(request, response)
  }
}