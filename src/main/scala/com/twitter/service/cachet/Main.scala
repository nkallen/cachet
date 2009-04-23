package com.twitter.service.cachet

import limiter.LimitingProxyServletFilter
import com.twitter.commons.W3CStats
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
    //Logger.get("").getHandlers().foreach(_.setLevel(Level.FINEST))
    // FIXME: put this into a threadpool section.
    ThreadPool.init(Configgy.config)

    val PROXY_PORT = Configgy.config.getInt("proxy_port", 1234)
    val GRACEFUL_MS = Configgy.config.getInt("graceful-shutdown-ms", 1000)
    val NUM_THREADS = Configgy.config.getInt("backend-numthreads", 100)
    var SSL_PORT = Configgy.config.getInt("ssl_port", 8433)
    var KEYSTORE_PASSWORD = Configgy.config.getString("keystore-password", "asdfasdf")
    // The path to your ssl keystore file.
    var KEYSTORE_LOCATION = Configgy.config.getString("keystore-location", "data/keystore")
    var SSL_PASSWORD = Configgy.config.getString("ssl-password", "asdfasdf")
    val server = new JettyServer(PROXY_PORT, GRACEFUL_MS, NUM_THREADS, SSL_PORT, KEYSTORE_PASSWORD,
                                 KEYSTORE_LOCATION, SSL_PASSWORD)
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
