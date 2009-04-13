package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import com.twitter.commons.W3CStats
import net.lag.configgy.{Config, Configgy, RuntimeEnvironment}
import net.lag.logging.Logger
import java.util.Properties

object Main {
  private val log = Logger.get
  private val runtime = new RuntimeEnvironment(getClass)

  def main(args: Array[String]) {
    runtime.load(args)

    val PROXY_PORT = Configgy.config.getInt("proxy_port", 1234)
    //val server = new JettyServer(PROXY_PORT)
    val server = new GSEServer(PROXY_PORT)
    log.info("Proxy Server listening on port: %s", PROXY_PORT)
    log.info(Stats.w3c.log_header)
    //server.addFilter(new LimitingProxyServletFilter, "/")
    val initParams = new Properties()
    // FIXME: make these configurable.
    initParams.put("backend-host", "localhost")
    initParams.put("backend-port", "80")
    initParams.put("backend-timeout", "1000")
    // FIXME: nail down how to pass all traffic through either a proxy or servlet using OpenGSE.
    //server.addFilter(classOf[BasicFilter], "/*")
    server.addFilter(classOf[LoggingFilter], "/")
    server.addServlet(classOf[ProxyServlet], "/", initParams)
    server.start()
    server.join()
  }
}

object Stats {
  val w3c = new W3CStats(Array("rs-response-time", "sc-response-code", "rs-response-code", "rs-response-method", "uri", "rs-content-type", "rs-content-length"))
}
