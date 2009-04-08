package com.twitter.service.cachet

import javax.servlet.Filter
import javax.servlet.http.HttpServlet
import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.Connector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool

/**
 * Class that allows easy embedding of Jetty.
 * How to use:
 * <code>
 *   val server = new Server(8080)
 *   server.start()
 * </code>
 */
class Server(port: Int) {
  protected val (server, context, connector) = configureServer()

  def addServlet(servlet: HttpServlet, route: String) {
    context.addServlet(new ServletHolder(servlet), route)
  }

  def addFilter(filter: Filter, route: String) {
    context.addFilter(new FilterHolder(filter), route, 1)
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
    val context = new Context(server, "/", Context.SESSIONS)
    val connector = newConnector
    val threadPool = new QueuedThreadPool
    // FIXME: make into a Configgy deal.
    threadPool.setMaxThreads(100)
    server.setThreadPool(threadPool)
    connector.setPort(port)
    server.setConnectors(Array(connector))
    (server, context, connector)
  }

  protected def newConnector: Connector = {
    new SelectChannelConnector
  }
}
