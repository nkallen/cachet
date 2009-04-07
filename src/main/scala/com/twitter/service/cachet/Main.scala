package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import net.lag.configgy.{Config, Configgy, RuntimeEnvironment}
import net.lag.logging.Logger

object Main {
  private val log = Logger.get
  private val runtime = new RuntimeEnvironment(getClass)

  def main(args: Array[String]) {
    runtime.load(args)

    val PROXY_PORT = Configgy.config.getInt("proxy_port", 1234)
    val server = new Server(PROXY_PORT)
    log.info("Proxy Server listening on port: %s", PORT)

    //server.addFilter(new LimitingProxyServletFilter, "/")
    server.addServlet(new ProxyServlet("localhost", 80, 5000), "/")
    server.start()
    server.join()
  }
}
