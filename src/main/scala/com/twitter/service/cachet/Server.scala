package com.twitter.service.cachet

import org.mortbay.jetty
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context}
import org.mortbay.thread.QueuedThreadPool
import servlet.{CacheProxyServletFilter, ProxyServlet}

class Server {
  val server = new jetty.Server
  val connector = new SelectChannelConnector
  val root = new Context(server, "/", Context.SESSIONS)
  val servlet = new ProxyServlet
  //  val filter = new CacheProxyServletFilter

  val threadPool = new QueuedThreadPool
  threadPool.setMaxThreads(100)
  server.setThreadPool(threadPool)

  connector.setPort(1234)
  server.setConnectors(Array(connector))

  //  root.addFilter(new FilterHolder(filter), "/", 1)
  root.addServlet(new ServletHolder(servlet), "/")

  def start() {
    server.start()
    server.join()
  }
}