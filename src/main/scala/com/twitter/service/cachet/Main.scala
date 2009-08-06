package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import com.twitter.service.W3CStats
import net.lag.configgy.{Config, Configgy, RuntimeEnvironment}
import net.lag.logging.Logger
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties
import java.util.logging.Level

object Main {
  private val log = Logger.get
  private val runtime = new RuntimeEnvironment(getClass)

  def main(args: Array[String]) {
    runtime.load(args)
    ThreadPool.init(Configgy.config)

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
  var w3c = new W3CStats(Logger.get, Array("rs-response-time", "sc-response-code", "rs-response-code", "rs-response-method", "uri", "rs-content-type", "rs-content-length", "remote-ip", "request-date", "request-time", "rs-went-away"))
}

trait ConfiggyInit {
  def init(config: Config)
}

object ThreadPool extends ConfiggyInit {
  val log = Logger.get
  var minThreads = 10
  var maxThreads = 250
  var maxIdleMS = 1000
  var lowThreads = 100

  def init(config: Config) {
    minThreads = Configgy.config.getInt("threadpool.min-threads", minThreads)
    maxThreads = Configgy.config.getInt("threadpool.max-threads", maxThreads)
    maxIdleMS = Configgy.config.getInt("threadpool.max-idle-ms", maxIdleMS)
    lowThreads = Configgy.config.getInt("threadpool.low-threads", lowThreads)
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
