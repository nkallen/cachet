package com.twitter.service.cachet

import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.Connector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.security.SslSelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties
import javax.net.ssl.SSLSocketFactory
import javax.servlet.Filter
import javax.servlet.http.HttpServlet
import net.lag.configgy.ConfigMap
import net.lag.logging.Logger

/**
 * Trait for exposing an ServletEngine.
 */
trait Server {
  /**
   * Defines a Port number to listen to HTTP requests on.
   */
  val port: Int

  /**
   * Defines how long jetty should wait, after being issued a shutdown request, before
   * dropping outstanding requests on the floor.
   */
  val gracefulShutdownMS: Int

  /**
   * the number of threads this http server should devote to listening to requests.
   */
  val numThreads: Int

  /**
   * Add a servlet with a set of initial properties to initial the Servlet with.
   */
  def addServlet(servlet: Class[_ <: HttpServlet], route: String, initial: Properties)

  def addServlet(servlet: Class[_ <: HttpServlet], route: String)

  /**
   * Adds a Servlet handled by the given routing url
   */
  def addServlet(servlet: HttpServlet, route: String)

  /**
   * Adds a ServletFilter for a given url initialized with a set of Properties
   */
  def addFilter(filter: Class[_ <: Filter], route: String, props: Properties)

  /**
   * Adds a Servlet Filter for handling a given routing url
   */
  def addFilter(filter: Filter, route: String)

  /**
   * Adds a Servlet Filter for handling a given routing url
   */
  def addFilter(filter: Class[_ <: Filter], route: String)

  /**
   * Start the ServletEngine
   */
  def start()

  /**
   * Stop the ServletEngine
   */
  def stop()

  /**
   * Join the servlet engines thread with your own.
   */
  def join()
}

/**
 * Class that allows easy embedding of Jetty.
 * How to use:
 * <code>
 *   val server = new JettyServer(8080)
 *   server.start()
 * </code>
 */
class JettyServer(val port: Int, val gracefulShutdownMS: Int, val numThreads: Int, val sslPorts: Seq[String],
                  val keystore_location: String, val keystore_password: String, val ssl_password: String, val acceptQueueSize: Int,
                  val threadConfig: ConfigMap, val connectorStats: Boolean) extends Server {
  private val log = Logger.get
  var acceptors = 2
  var maxIdleTimeMS = 1000
  var lowResourcesMaxIdleTimeMS = 300
  var lowResourcesConnections = 200
  var reuseAddress = true
  var headerBufferSize = 4192
  var requestBufferSize = 16 * 1024
  var responseBufferSize = 16 * 1024

  if (threadConfig != null) {
    ThreadPool.init(threadConfig)
  }

  val (server, context, connector) = configureHttp()
  val connectors = configureSsl()

  def this(config: ConfigMap) {
    this(config.getInt("port", 8080), config.getInt("gracefulShutdownMS", 1000), config.getInt("backend-num-threads", 10),
         config.getList("ssl-ports"), config.getString("keystore-location", "notset"),
         config.getString("keystore-password", "notset"), config.getString("ssl-password", "notset"), config.getInt("accept-queue-size", 512),
         config.configMap("threadpool"), config.getBool("connector.collectStats", false))
    acceptors = config.getInt("connector.acceptors", acceptors)
    reuseAddress = config.getBool("connector.reuseAddress", true)
    maxIdleTimeMS = config.getInt("connector.maxIdleTimeMS", maxIdleTimeMS)
    lowResourcesMaxIdleTimeMS = config.getInt("connector.lowResourcesMaxIdleTimeMS", lowResourcesMaxIdleTimeMS)
    lowResourcesConnections = config.getInt("connector.lowResourcesConnections", lowResourcesConnections)
    headerBufferSize = config.getInt("connector.headerBufferSize", headerBufferSize)
    requestBufferSize = config.getInt("connector.requestBufferSize", requestBufferSize)
    responseBufferSize = config.getInt("connector.responseBufferSize", responseBufferSize)

    log.info("Initializing JettyServer with options: port:%s, gracefulShutdownMS:%s, numThreads:%s, sslPort:%s, keystore_location:%s "
             .format(port, gracefulShutdownMS, numThreads, sslPorts, keystore_location))
    log.info("[more options] acceptors:%s maxIdleTimeMS:%s lowResourcesMaxIdleTimeMS:%s lowResourcesConnections:%s"
             .format(acceptors, maxIdleTimeMS, lowResourcesMaxIdleTimeMS, lowResourcesConnections))
    config.getConfigMap("ssl") match {
      case Some(ssl) => ssl.keys.foreach { domain =>
        val props = ssl.getList(domain)
        val port = props(0).toInt
        val keystore = props(1)
        log.info("adding SSL Connector on port %s for domain %s with keystore %s", port, domain, keystore)
        server.addConnector(newSslConnector(port, keystore))
      }
      case None => log.info("No SSL connectors being installed")
    }

  }

  def addServlet(servlet: Class[_ <: HttpServlet], route: String, props: Properties) {
    val holder = new ServletHolder(servlet)
    holder.setInitParameters(props)
    context.addServlet(holder, route)
  }

  def addFilter(filter: Class[_ <: Filter], route: String, props: Properties) {
    val holder = new FilterHolder(filter)
    holder.setInitParameters(props)
    context.addFilter(holder, route, 1)
  }

  def addFilter(filter: Class[_ <: Filter], route: String) {
    addFilter(filter, route, new Properties())
  }

  def addServlet(servlet: Class[_ <: HttpServlet], route: String) {
    addServlet(servlet, route, new Properties)
  }

  def addServlet(servlet: HttpServlet, route: String) {
    context.addServlet(new ServletHolder(servlet), route)
  }

  def addFilter(filter: Filter, route: String) {
    addFilter(filter.getClass.asInstanceOf[Filter], route)
  }

  def start() {
    server.start()
  }

  def join() {
    server.join()
  }

  def stop() {
    server.stop()
  }

  private def configureHttp() = {
    val server = new jetty.Server
    server.setGracefulShutdown(gracefulShutdownMS)
    val context = new Context(server, "/", Context.SESSIONS)
    server.setThreadPool(ThreadPool())
    try {
      val q = server.getThreadPool.asInstanceOf[QueuedThreadPool]
      val name = q.getName
      val min = q.getMinThreads
      val max = q.getMaxThreads
      val low = q.getLowThreads
      val idle = q.getIdleThreads
      val maxIdle = q.getMaxIdleTimeMs
      val queued = q.getMaxQueued
      log.info("QueuedThreadPool in use: name: %s, min: %s, max: %s, low: %s idle: %s, max-idle: %s, max-queued: %s"
               .format(name, min, max, low, idle, maxIdle, queued))
    } catch {
      case e => log.error(e, "ThreadPool is not a QueuedThreadPool")
    }
    val connector = newHttpConnector
    server.setConnectors(Array(connector))
    (server, context, connector)
  }

  def addListener(config: ListenConfig) = {
    server.addConnector(newHttpConnector(config.port, config.domain, Some(config.ip)))
    config.sslPort match {
      case Some(port) => server.addConnector(newSslConnector(port, config.sslKeystore, Some(config.ip)))
      case None =>
    }
  }

  def newHttpConnector: Connector = newHttpConnector(port, "default", None)

  def newHttpConnector(port: Int, name: String, host: Option[String]): Connector = {
    log.info("returning new HTTP Connector on port %s for host %s", port, host)
    val conn = new SelectChannelConnector
    conn.setPort(port)
    host.foreach { ip =>
      conn.setHost(ip)
      conn.setName(name + "-connector")
    }
    conn.setAcceptors(acceptors)
    conn.setMaxIdleTime(maxIdleTimeMS)
    conn.setAcceptQueueSize(acceptQueueSize)
    conn.setStatsOn(connectorStats)
    conn.setLowResourcesConnections(lowResourcesConnections)
    conn.setLowResourceMaxIdleTime(lowResourcesMaxIdleTimeMS)
    conn.setResolveNames(false)
    conn.setReuseAddress(reuseAddress)
    conn.setHeaderBufferSize(headerBufferSize)
    conn.setRequestBufferSize(requestBufferSize)
    conn.setResponseBufferSize(responseBufferSize)

    log.info("Jetty accept queue size: %s", conn.getAcceptQueueSize)
    log.info("Jetty SO_LINGER time: %s", conn.getSoLingerTime)
    log.info("Jetty connections open max: %s", conn.getConnectionsOpenMax)
    log.info("Jetty max idle time: %s", conn.getMaxIdleTime)
    log.info("Jetty low resources max idle time: %s", conn.getLowResourceMaxIdleTime)
    log.info("Jetty header buffer size: %s", conn.getHeaderBufferSize)
    log.info("Jetty response buffer size: %s", conn.getResponseBufferSize)
    log.info("Jetty request buffer size: %s", conn.getRequestBufferSize)
    log.info("Jetty acceptor priority offset: %s", conn.getAcceptorPriorityOffset)
    log.info("Jetty stats are on: " + conn.getStatsOn)
    log.info("Jetty stats (in Ms) are on: " + conn.getStatsOnMs)
    conn
  }

  def newSslConnector(port: Int, keystoreLocation: String): Connector =
    newSslConnector(port, keystoreLocation, None)

  def newSslConnector(port: Int, keystoreLocation: String, host: Option[String]): Connector = {
    val conn = new SslSelectChannelConnector
    conn.setKeystore(keystoreLocation)
    conn.setKeyPassword(keystore_password)
    conn.setPassword(ssl_password)
    conn.setNeedClientAuth(false)
    conn.setWantClientAuth(false)
    if (conn.getExcludeCipherSuites != null) {
      log.info("SSL Connector excluded cipher suites: " + conn.getExcludeCipherSuites().mkString("[", ",", "]"))
    } else {
      log.info("SSL Connector found no excluded cipher suites")
    }
    conn.setPort(port)
    host.foreach { name =>
      conn.setHost(name)
      conn.setName(name + "-connector-ssl")
    }
    conn.setAcceptors(acceptors)
    conn.setMaxIdleTime(maxIdleTimeMS)
    conn.setStatsOn(connectorStats)
    conn.setResolveNames(false)
    conn.setReuseAddress(reuseAddress)

    try {
      val suites = SSLSocketFactory.getDefault.asInstanceOf[SSLSocketFactory].getDefaultCipherSuites
      log.info("We support the following SSL cipher suites: " + suites.mkString(","))
    } catch {
      case e: Exception => log.error(e, "Problem fetching cipher suites")
    }
    log.info("SSL Connector Protocol: " + conn.getProtocol())
    // We exclude these ciphers as they cause issues.
    conn.setExcludeCipherSuites(Array("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                                      "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                                      "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"))

    conn.setLowResourcesConnections(lowResourcesConnections)
    conn.setLowResourceMaxIdleTime(lowResourcesMaxIdleTimeMS)
    conn
  }

  def newSslConnector(port: Int): Connector = newSslConnector(port, keystore_location)

  def configureSsl() = sslPorts.map { port =>
    log.info("adding SSL connector for port %s", port)
    val ssl = newSslConnector(port.toInt)
    server.addConnector(ssl)
    ssl
  }
}

case class ListenConfig(domain: String, ip: String, port: Int, sslPort: Option[Int], sslKeystore: String)
