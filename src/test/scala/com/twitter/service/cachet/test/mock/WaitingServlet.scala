package com.twitter.service.cachet.test.mock

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class WaitingServlet(milliseconds: Long) extends HttpServlet {
  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    Thread.sleep(milliseconds)
    response.setStatus(HttpServletResponse.SC_OK)
  }
}