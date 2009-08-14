package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import com.twitter.service.W3CStats
import com.twitter.service.Stats._
import net.lag.configgy.{Config, ConfigMap, Configgy, RuntimeEnvironment}
import net.lag.logging.Logger
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties
import java.util.logging.Level

object Main {
  private val log = Logger.get
  private val runtime = new RuntimeEnvironment(getClass)

  def main(args: Array[String]) {
    runtime.load(args)

    val server = new JettyServer(Configgy.config)
    log.info(Stats.w3c.log_header)

    val initParams = new Properties()
    initParams.put("backend-host", Configgy.config.getString("backend-host", "localhost"))
    initParams.put("backend-port", Configgy.config.getString("backend-port", "80"))
    initParams.put("backend-timeout", Configgy.config.getString("backend-timeout", "4000"))
    initParams.put("backend-numthreads", Configgy.config.getInt("backend-num-threads", 100).toString)
    server.addServlet(classOf[ProxyServlet], "/", initParams)
    server.start()
    server.join()
  }
}

object Stats {
  StatsMBean("com.twitter.cachet")
  var w3c = new W3CStats(Logger.get, Array("rs-response-time", "sc-response-code", "rs-response-code", "rs-response-method", "x-protocol", "host", "uri", "rs-content-type", "rs-content-length", "remote-ip", "request-date", "request-time", "rs-went-away", "x-proxy-id"))
  val requestsHandled = buildIncr("requestsHandled")
  val returned2xx = buildIncr("returned2xx")
  val returned3xx = buildIncr("returned3xx")
  val returned4xx = buildIncr("returned4xx")
  val returned5xx = buildIncr("returned5xx")
  val clientResponseSent = buildIncr("clientResponseSent")
  val clientLeftEarly = buildIncr("clientLeftEarly")
  // We couldn't find a backend proxy for a Host
  val noProxyFoundForHost = buildIncr("noProxyFoundForHost")
  // We couldn't find a Host header in the request
  val noHostFound = buildIncr("noHostFound")


  def countRequestsForHost(name: String) = incr("host_%s".format(name), 1)
}

object ThreadPool {
  val log = Logger.get
  var minThreads = 10
  var maxThreads = 250
  var maxIdleMS = 1000
  var lowThreads = 100

  def init(config: ConfigMap) {
    minThreads = config.getInt("min-threads", minThreads)
    maxThreads = config.getInt("max-threads", maxThreads)
    maxIdleMS = config.getInt("max-idle-ms", maxIdleMS)
    lowThreads = config.getInt("low-threads", lowThreads)
    log.info("initializing ThreadPool values from Configgy: minThreads: %s, " +
             "maxThreads: %s, maxIdleMS: %s, lowThreads: %s"
             .format(minThreads, maxThreads, maxIdleMS, lowThreads))
  }

  def apply(numThreads: Int) = {
    val threadPool = new QueuedThreadPool(numThreads)
    threadPool.setMinThreads(minThreads)
    threadPool.setLowThreads(lowThreads)
    threadPool.setMaxThreads(maxThreads)
    threadPool.setMaxIdleTimeMs(maxIdleMS)
    threadPool.setDaemon(true)
    threadPool
  }
}
