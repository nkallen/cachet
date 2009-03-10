package com.twitter.service.cachet.test.integration

import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import org.specs.Specification

object ProxyServletSpec extends Specification {
  "ProxyServlet" >> {
    "when the backend is slow" >> {
      skip("asdf")
      val proxyServer = new TestServer(2345)
      proxyServer.addServlet(new ProxyServlet("localhost", 3456, 100), "/")
      proxyServer.start()

      val slowServer = new Server(3456)
      slowServer.addServlet(new WaitingServlet(200), "/")
      slowServer.start()

      val request = new HttpTester
      val response = new HttpTester
      request.addHeader("X-Forwarded-For", "1.1.1.1")
      request.setMethod("GET")
      request.setURI("/")
      request.setVersion("HTTP/1.0")
      response.parse(proxyServer(request.generate))
      response.getStatus mustEqual 200

      proxyServer.stop()
      slowServer.stop()
    }
  }
}