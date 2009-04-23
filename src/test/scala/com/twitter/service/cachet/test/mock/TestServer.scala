package com.twitter.service.cachet.test.mock

import org.mortbay.jetty.LocalConnector

class TestServer(port: Int, gracefulShutdown: Int, numThreads: Int, ssl_port: Int,
                 keystore_location: String, keystore_password: String, ssl_password: String)
    extends JettyServer(port, gracefulShutdown, numThreads, ssl_port, keystore_location, keystore_password, ssl_password) {
  def apply(request: String) = {
    val c = connector.asInstanceOf[LocalConnector]
    c.reopen()
    c.getResponses(request)
  }

  override def newHttpConnector = {
    val connector = new LocalConnector
    connector.setPort(port)
    connector
  }
}
