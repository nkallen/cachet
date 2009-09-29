package com.twitter.service.cachet.test.integration

import com.twitter.service.cachet.proxy.BufferedRequestWrapper
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

  class ReadParameterServletFilter extends Filter {
    def init(c: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      val bufferedRequest = new BufferedRequestWrapper(request.asInstanceOf[HttpServletRequest])
      chain.doFilter(bufferedRequest, response)
    }

    def destroy {}
  }

  def makeRequestThroughProxy(method: String, queryString: String): HttpTester = {
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
    if (method == "POST") {
      request.setURI("/")
      request.setContent(queryString)
      request.addHeader("Content-Type", "application/x-www-form-urlencoded")
    } else {
      request.setURI("/" + "?" + queryString)
    }

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

      makeRequestThroughProxy("POST", "hello=goodbye").getStatus mustEqual HttpServletResponse.SC_OK
      makeRequestThroughProxy("POST", "").getStatus mustEqual HttpServletResponse.SC_BAD_REQUEST
    }
  }
}
