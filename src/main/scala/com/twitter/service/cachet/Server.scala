package com.twitter.service.cachet

import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.servlet.{ServletHolder, Context}

class Server {
  val server = new org.mortbay.jetty.Server
  val connector = new SocketConnector
  val root = new Context(server, "/", Context.SESSIONS)
  val servlet = new ProxyServlet

  connector.setPort(1234)
  server.setConnectors(Array(connector))

  root.addServlet(new ServletHolder(servlet), "/")

  def start() {
    server.start()
    server.join()
  }
}