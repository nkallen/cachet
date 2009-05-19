package com.twitter.service.cachet

import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.Connector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.security.SslSelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties
import javax.servlet.Filter
import javax.servlet.http.HttpServlet
import net.lag.configgy.Config
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
class JettyServer(val port: Int, val gracefulShutdownMS: Int, val numThreads: Int, val ssl_port: Int,
                  val keystore_location: String, val keystore_password: String, val ssl_password: String) extends Server {
  private val log = Logger.get
  var acceptors = 1
  var maxIdleTimeMS = 100
  var lowResourcesMaxIdleTimeMS = 1000
  var lowResourcesConnections = 100

  def this(config: Config) {
    this(config.getInt("port", 8080), config.getInt("gracefulShutdownMS", 10), config.getInt("backend-num-threads", 10),
         config.getInt("ssl-port", 433), config.getString("keystore-location", "notset"),
         config.getString("keystore-password", "notset"), config.getString("ssl-password", "notset"))
    acceptors = config.getInt("connector.acceptors", acceptors)
    maxIdleTimeMS = config.getInt("connector.maxIdleTimeMS", maxIdleTimeMS)
    lowResourcesMaxIdleTimeMS = config.getInt("connector.lowResourcesMaxIdleTimeMS", lowResourcesMaxIdleTimeMS)
    lowResourcesConnections = config.getInt("connector.lowResourcesConnections", lowResourcesConnections)
    log.info("initilizing JettyServer with the following: port:%s, gracefulShutdownMS:%s, numThreads:%s, ssl_port:%s, keystore_location:%s acceptors:%s maxIdleTimeMS:%s lowResourcesMaxIdleTimeMS:%s lowResourcesConnections:%s".format(port, gracefulShutdownMS, numThreads, ssl_port, keystore_location, acceptors, maxIdleTimeMS, lowResourcesMaxIdleTimeMS, lowResourcesConnections))
  }

  val (server, context, connector, sslConnector) = configureServer()

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
    addFilter(filter, route)
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

  private def configureServer() = {
    val server = new jetty.Server
    server.setGracefulShutdown(gracefulShutdownMS)
    val context = new Context(server, "/", Context.SESSIONS)
    val threadPool = new QueuedThreadPool
    // FIXME: make into a Configgy deal.
    server.setThreadPool(ThreadPool(numThreads))
    val connector = newHttpConnector
    val sslConnector = newSslConnector
    server.setConnectors(Array(connector, sslConnector))
    (server, context, connector, sslConnector)
  }

  protected def newHttpConnector: Connector = {
    val conn = new SelectChannelConnector
    conn.setPort(port)
    conn.setAcceptors(acceptors)
    conn.setMaxIdleTime(maxIdleTimeMS)
    conn.setStatsOn(false)
    conn.setLowResourcesConnections(lowResourcesConnections)
    conn.setLowResourcesMaxIdleTime(lowResourcesMaxIdleTimeMS)
    conn
  }

  protected def newSslConnector: Connector = {
    val conn = new SslSelectChannelConnector
    conn.setKeystore(keystore_location)
    conn.setKeyPassword(keystore_password)
    conn.setPassword(ssl_password)
    conn.setPort(ssl_port)
    conn.setAcceptors(acceptors)
    conn.setMaxIdleTime(maxIdleTimeMS)
    conn.setStatsOn(false)
    conn.setLowResourcesConnections(lowResourcesConnections)
    conn.setLowResourcesMaxIdleTime(lowResourcesMaxIdleTimeMS)
    conn
  }
}


