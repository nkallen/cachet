package com.twitter.service.cachet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class Servlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.setContentType("text/plain")
    resp.setStatus(200)
    resp.getWriter().println("Hello World!")
  }
}