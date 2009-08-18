package com.twitter.service.cachet.test.integration

import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import java.util.Properties
import javax.servlet.http.HttpServletResponse
import org.specs.Specification

object MultiBackendProxyServletSpec extends Specification {

  val host1 = ProxyBackendConfig("host1.com", "127.0.0.1", 80, 443, Nil)
  val host2 = ProxyBackendConfig("host2.com", "127.0.0.2", 8080, 8443, Nil)

  "MultiBackendProxyServlet" should {
    "parse correctly formatted Properties file" >> {
      val backendMap = BackendsToProxyMap(List(host1, host2), 0, 0, 0, "/tmp", "w3c.log")
      backendMap.keySet.size mustEqual 2
      backendMap.containsKey("host1.com") mustEqual true
      backendMap.containsKey("host2.com") mustEqual true
    }

    "handle wildcard subdomain support" >> {
      HostRouter.setHosts(BackendsToProxyMap(List(host1, host2), 0, 0, 0, "/tmp", "w3c.log"))
      HostRouter("host1.com") mustNot beNull
      HostRouter.backendMap.containsKey("foo.host1.com") mustBe false
      HostRouter("foo.host1.com") mustNot beNull
      HostRouter("host1.com") mustEqual HostRouter("foo.host1.com")
      HostRouter("host1.com:9000") mustEqual HostRouter("host1.com")
      HostRouter("something.host1.com:9000") mustEqual HostRouter("host1.com")
    }
  }
}
