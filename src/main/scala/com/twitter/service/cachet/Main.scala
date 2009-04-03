package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter

object Main {
  def main(args: Array[String]) {
    val server = new Server(1234)
    //server.addFilter(new LimitingProxyServletFilter, "/")
    server.addServlet(new ProxyServlet("localhost", 80, 5000), "/")
    server.start()
    server.join()
  }
}
