package com.twitter.service.cachet.test.mock

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class WaitingServlet extends HttpServlet {
  var milliseconds = 0L

  override def init(config: ServletConfig) {
    milliseconds = config.getInitParameter("timeout") match {
      case null => 1000L
      case x: String => x.toLong
    }
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
   Thread.sleep(milliseconds)
    response.setStatus(HttpServletResponse.SC_OK)
  }
}
