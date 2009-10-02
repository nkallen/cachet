package com.twitter.service.cachet.test.integration

import com.twitter.service.cachet.proxy.BufferedRequestWrapper
import com.twitter.service.cachet.test.mock.FakeHttpServletRequest
import limiter.LimitingProxyServletFilter
import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.testing.HttpTester
import org.specs.Specification
import java.util.Properties
import javax.servlet._
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


object ProxyServletFilterSpec extends Specification {

  /**
   * Returns status 200 if the expected parameter exists, 400 otherwise.
   */
  class ExpectsParameterServlet extends HttpServlet {
    var parameter = ""
    override def init(config: ServletConfig) {
      parameter = config.getInitParameter("parameter") match {
        case null => "hello"
        case s: String => s
      }
    }

    override def service(request: HttpServletRequest, response: HttpServletResponse) {
      if (request.getParameter(parameter) != null) {
        response.setStatus(HttpServletResponse.SC_OK)
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      }
    }
  }

  /**
   * Instantiates a BufferedRequestWrapper only if it's a POST request so we can read the body multiple times.
   */
  class ReadParameterServletFilter extends Filter {
    def init(c: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      val req = request.asInstanceOf[HttpServletRequest]
      val servletRequest = if (req.getMethod == "POST") {
        new BufferedRequestWrapper(req)
      } else {
        req
      }

      if (servletRequest.getParameter("hello") == null) {
        response.asInstanceOf[HttpServletResponse].setStatus(HttpServletResponse.SC_BAD_REQUEST)
      } else {
        chain.doFilter(servletRequest, response)
      }
    }

    def destroy {}
  }

  def makeRequestThroughProxy(method: String, queryStringUrl: String): HttpTester =
    makeRequestThroughProxy(method, queryStringUrl, None)

  def makeRequestThroughProxy(method: String, queryStringUrl: String, queryStringBody: Option[String]): HttpTester = {
    var i = 0
    val proxyServer = new TestServer(2345+i, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf")
    val proxyProps = new Properties()
    proxyProps.put("backend-port", (3000+i).toString)
    proxyProps.put("backend-ssl-port", "8443")
    proxyServer.addServlet(classOf[ProxyServlet], "/", proxyProps)
    proxyServer.addFilter(classOf[ReadParameterServletFilter], "/*")
    proxyServer.start()

    val slowServer = new JettyServer(3000+i, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf", 10, null, false)
    val expectingProps = new Properties()
    expectingProps.put("parameter", "hello")
    slowServer.addServlet(classOf[ExpectsParameterServlet], "/", expectingProps)
    i += 1 // to not run into address binding issues
    slowServer.start()

    val request = new HttpTester
    val response = new HttpTester
    request.addHeader("X-Forwarded-For", "1.1.1.1")
    request.setMethod(method)
    queryStringUrl match {
      case "" => request.setURI("/")
      case q: String => request.setURI("/?" + q)
    }

    queryStringBody.foreach { q => request.setContent(q) }
    if (request.getMethod == "POST") request.addHeader("Content-Type", "application/x-www-form-urlencoded")

    request.setVersion("HTTP/1.0")
    response.parse(proxyServer(request.generate))

    proxyServer.stop()
    slowServer.stop()
    response
  }

  "ProxyServletFilterSpec" >> {
    "does not eat the InputStream" >> {
      // make a request, check on the inputstream.
      makeRequestThroughProxy("GET", "hello=goodbye").getStatus mustEqual HttpServletResponse.SC_OK
      makeRequestThroughProxy("GET", "").getStatus mustEqual HttpServletResponse.SC_BAD_REQUEST

      makeRequestThroughProxy("POST", "", Some("hello=goodbye")).getStatus mustEqual HttpServletResponse.SC_OK
      makeRequestThroughProxy("POST", "").getStatus mustEqual HttpServletResponse.SC_BAD_REQUEST

      makeRequestThroughProxy("POST", "hello=goodbye", Some("hi=bye")).getStatus mustEqual HttpServletResponse.SC_OK
      makeRequestThroughProxy("POST", "hi=bye", Some("hello=goodbye")).getStatus mustEqual HttpServletResponse.SC_OK
      makeRequestThroughProxy("POST", "hello=goodbye").getStatus mustEqual HttpServletResponse.SC_OK
    }

    "can parse a query string" >> {
      val request = new FakeHttpServletRequest()
      val buf = new BufferedRequestWrapper(request)
      buf.parseQueryString(null) mustEqual Map.empty
      buf.parseQueryString("hello") mustEqual Map.empty
      buf.parseQueryString("hello=goodbye") must containAll(Map("hello" -> "goodbye"))
      buf.parseQueryString("hello=goodbye&hi=bye") must containAll(Map("hello" -> "goodbye", "hi" -> "bye"))
    }
  }
}
