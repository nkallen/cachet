package com.twitter.service.cachet.test.integration

import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import java.util.Properties
import javax.servlet.http.HttpServletResponse
import org.specs.Specification

object MultiBackendProxyServletSpec extends Specification {

  val props = new Properties()
  // host1.com
  props.put("host1.com.ip", "127.0.0.1")
  props.put("host1.com.port", "80")
  props.put("host1.com.ssl-port", "443")
  // host2.com
  props.put("host2.com.ip", "127.0.0.2")
  props.put("host2.com.port", "8080")
  props.put("host2.com.ssl-port", "8443")

  "MultiBackendProxyServlet" should {
    "parse correctly formatted Properties file" >> {
      val backendMap = BackendsToProxyMap(props, 0, 0, 0, "/tmp", "w3c.log")
      backendMap.containsKey("host1.com") mustEqual true
      backendMap.containsKey("host2.com") mustEqual true
    }
  }
}
