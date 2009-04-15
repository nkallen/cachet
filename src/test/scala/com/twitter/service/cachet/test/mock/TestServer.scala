package com.twitter.service.cachet.test.mock

import org.mortbay.jetty.LocalConnector

class TestServer(port: Int, gracefulShutdown: Int, numThreads: Int) extends JettyServer(port, gracefulShutdown, numThreads) {
  def apply(request: String) = {
    val c = connector.asInstanceOf[LocalConnector]
    c.reopen()
    c.getResponses(request)
  }

  override def newConnector = {
    val connector = new LocalConnector
    connector.setPort(port)
    connector
  }
}
