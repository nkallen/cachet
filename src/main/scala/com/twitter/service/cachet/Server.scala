package com.twitter.service.cachet

import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool
import servlet.{CacheProxyServletFilter, ProxyServlet}

class Server {
  val (server, context) = configureServer()
  //  configureCacheProxy(context)
  configureProxyServlet(context)

  def start() {
    server.start()
    server.join()
  }

  private def configureServer() = {
    val server = new jetty.Server
    val context = new Context(server, "/", Context.SESSIONS)
    val connector = new SelectChannelConnector
    val threadPool = new QueuedThreadPool
    threadPool.setMaxThreads(100)
    server.setThreadPool(threadPool)
    connector.setPort(1234)
    server.setConnectors(Array(connector))
    (server, context)
  }

  private def configureProxyServlet(context: Context) {
    val servlet = new ProxyServlet
    val servletHolder = new ServletHolder(servlet)
    servletHolder.setInitParameter("host", "localhost")
    servletHolder.setInitParameter("port", "80")
    context.addServlet(servletHolder, "/")
  }

  private def configureCacheProxy(context: Context) {
    val filter = new CacheProxyServletFilter
    context.addFilter(new FilterHolder(filter), "/", 1)
  }
}