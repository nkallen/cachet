package com.twitter.service.cachet.test.integration

import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import javax.servlet.http.HttpServletResponse
import org.specs.Specification

object ProxyServletSpec extends Specification {
  "ProxyServlet" >> {
    "when the backend is slow" >> {
      val proxyServer = new TestServer(2345)
      proxyServer.addServlet(new ProxyServlet("localhost", 3456, 100), "/")
      proxyServer.start()

      val slowServer = new Server(3456)
      slowServer.addServlet(new WaitingServlet(20000), "/")
      slowServer.start()

      val request = new HttpTester
      val response = new HttpTester
      request.addHeader("X-Forwarded-For", "1.1.1.1")
      request.setMethod("GET")
      request.setURI("/")
      request.setVersion("HTTP/1.0")
      response.parse(proxyServer(request.generate))
      response.getStatus mustEqual HttpServletResponse.SC_SERVICE_UNAVAILABLE

      proxyServer.stop()
      slowServer.stop()
    }
  }
}