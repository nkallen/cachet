package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import com.twitter.commons.W3CStats
import net.lag.configgy.{Config, Configgy, RuntimeEnvironment}
import net.lag.logging.Logger
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties

object Main {
  private val log = Logger.get
  private val runtime = new RuntimeEnvironment(getClass)

  def main(args: Array[String]) {
    runtime.load(args)
    ThreadPool.init(Configgy.config)

    val PROXY_PORT = Configgy.config.getInt("proxy_port", 1234)
    val GRACEFUL_MS = Configgy.config.getInt("graceful-shutdown-ms", 1000)
    val NUM_THREADS = Configgy.config.getInt("backend-numthreads", 100)
    val server = new JettyServer(PROXY_PORT, GRACEFUL_MS, NUM_THREADS)
    //val server = new GSEServer(PROXY_PORT)
    log.info("Proxy Server listening on port: %s", PROXY_PORT)
    log.info(Stats.w3c.log_header)
    //server.addFilter(new LimitingProxyServletFilter, "/")
    val initParams = new Properties()
    initParams.put("backend-host", Configgy.config.getString("backend-host", "localhost"))
    initParams.put("backend-port", Configgy.config.getString("backend-port", "80"))
    initParams.put("backend-timeout", Configgy.config.getString("backend-timeout", "4000"))
    initParams.put("backend-numthreads", NUM_THREADS.toString)
    server.addServlet(classOf[ProxyServlet], "/", initParams)
    server.start()
    server.join()
  }
}

object Stats {
  val w3c = new W3CStats(Array("rs-response-time", "sc-response-code", "rs-response-code", "rs-response-method", "uri", "rs-content-type", "rs-content-length", "remote-ip", "request-date", "request-time"))
}

trait ConfiggyInit {
  def init(config: Config)
}

object ThreadPool extends ConfiggyInit {
  val log = Logger.get
  var minThreads = 10
  var maxThreads = 250
  var maxIdleMS = 1000

  def init(config: Config) {
    log.info("initializing ThreadPool values from Configgy")
    minThreads = Configgy.config.getInt("threadpool-min-threads", 10)
    maxThreads = Configgy.config.getInt("threadpool-max-threads", 250)
    maxIdleMS = Configgy.config.getInt("threadpool-min-threads", 10)
  }

  def apply(numThreads: Int) = {
    val threadPool = new QueuedThreadPool(numThreads)
    threadPool.setMinThreads(minThreads)
    threadPool.setMaxThreads(maxThreads)
    threadPool.setMaxIdleTimeMs(maxIdleMS)
    threadPool.setDaemon(true)
    threadPool
  }
}
