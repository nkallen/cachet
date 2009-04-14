package com.twitter.service.cachet

import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.Connector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool
import java.util.Properties
import javax.servlet.Filter
import javax.servlet.http.HttpServlet

/**
 * Trait for exposing an ServletEngine.
 */
trait Server {
  /**
   * Defines a Port number to listen to HTTP requests on.
   */
  val port: Int

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
 *   val server = new Server(8080)
 *   server.start()
 * </code>
 */
class JettyServer(val port: Int) extends Server {
  protected val (server, context, connector) = configureServer()

  def addServlet(servlet: Class[_ <: HttpServlet], route: String, props: Properties) {
    val holder = new ServletHolder(servlet)
    holder.setInitParameters(props)
    context.addServlet(holder, route)
  }

  def addFilter(filter: Class[_ <: Filter], route: String) {
    context.addFilter(new FilterHolder(filter), route, 1)
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
    // FIXME: make this configurable.
    server.setGracefulShutdown(1000)
    val context = new Context(server, "/", Context.SESSIONS)
    val connector = newConnector
    val threadPool = new QueuedThreadPool
    // FIXME: make into a Configgy deal.
    threadPool.setMinThreads(10)
    threadPool.setMaxThreads(250)
    threadPool.setMaxIdleTimeMs(1000)
    threadPool.setDaemon(true)
    server.setThreadPool(threadPool)
    connector.setPort(port)
    server.setConnectors(Array(connector))
    (server, context, connector)
  }

  protected def newConnector: Connector = {
    new SelectChannelConnector
  }
}
