package com.twitter.service.cachet.test.integration

import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import java.util.Properties
import javax.servlet.http.HttpServletResponse
import org.specs.Specification

object ProxyServletSpec extends Specification {
  var i = 0

  def makeRequestThroughProxy(sleepTime: Long, method: String): HttpTester = {
    val proxyServer = new TestServer(2345+i, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf")
    val proxyProps = new Properties()
    proxyProps.put("backend-port", (3000+i).toString)
    proxyProps.put("backend-ssl-port", "8443")
    proxyServer.addServlet(classOf[ProxyServlet], "/", proxyProps)
    proxyServer.start()

    val slowServer = new JettyServer(3000+i, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf", 10, null)
    val waitingProps = new Properties()
    waitingProps.put("timeout", sleepTime.toString)
    slowServer.addServlet(classOf[WaitingServlet], "/", waitingProps)
    i += 1 // to not run into address binding issues
    slowServer.start()

    val request = new HttpTester
    val response = new HttpTester
    request.addHeader("X-Forwarded-For", "1.1.1.1")
    request.setMethod(method)
    request.setURI("/")
    request.setVersion("HTTP/1.0")
    response.parse(proxyServer(request.generate))

    proxyServer.stop()
    slowServer.stop()
    response
  }

  // By default, do a GET request
  def makeRequestThroughProxy(sleepTime: Long): HttpTester = {
    makeRequestThroughProxy(sleepTime, "GET")
  }

  "ProxyServlet" >> {
    "when the backend too slow" >> {
      "it times out the response, returning HTTP 504" >> {
        val response = makeRequestThroughProxy(2000)
        response.getStatus mustEqual HttpServletResponse.SC_GATEWAY_TIMEOUT
      }
    }

    "when the backend is fast" >> {
      "it propagates the response" >> {
        val response = makeRequestThroughProxy(0)
        response.getStatus mustEqual HttpServletResponse.SC_OK
      }
    }

    "when a HEAD request is made (slow version)" >> {
      "it times out the response, returning HTTP 504" >> {
        val response = makeRequestThroughProxy(2000, "HEAD")
        response.getStatus mustEqual HttpServletResponse.SC_GATEWAY_TIMEOUT
      }
    }

    "when a HEAD request is made (fast version)" >> {
      "it propagates the response" >> {
        val response = makeRequestThroughProxy(0, "HEAD")
        response.getStatus mustEqual HttpServletResponse.SC_OK
      }
    }
  }
}
