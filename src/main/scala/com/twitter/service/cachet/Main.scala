package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import java.util.Properties

object Main {
  def main(args: Array[String]) {
    val server = new GSEServer(1234)
    //server.addFilter(new LimitingProxyServletFilter, "/")
    val initParams = new Properties()
    // FIXME: make these configurable.
    initParams.put("backend-host", "localhost")
    initParams.put("backend-port", "80")
    initParams.put("backend-timeout", "1000")
    // FIXME: nail down how to pass all traffic through either a proxy or servlet using OpenGSE.
    //server.addFilter(classOf[BasicFilter], "/*")
    server.addServlet(classOf[ProxyServlet], "/", initParams)
    server.start()
    server.join()
  }
}
