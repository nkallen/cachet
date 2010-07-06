package com.twitter.service.cachet.test.integration

import mock.{WaitingServlet, TestServer}
import org.mortbay.jetty.handler.ErrorHandler
import org.mortbay.jetty.servlet.ErrorPageErrorHandler
import org.mortbay.jetty.testing.HttpTester
import java.util.Properties
import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.specs.Specification

object ServerConfigSpec extends Specification {
  var i = 0

  class ErrorServlet extends HttpServlet {
    var code = 500

    override def init(config: ServletConfig) {
      code = config.getInitParameter("code") match {
        case null => 200
        case x: String => x.toInt
      }
    }

    override def service(request: HttpServletRequest, response: HttpServletResponse) {
      if (code >= 400) {
        response.sendError(code)
      } else {
        response.setStatus(code)
      }
    }
  }

  def makeRequestThroughErrorServlet(request: HttpTester, errorHandlerMap: Map[Int, String], code: Int): HttpTester = {
    val badServer = new TestServer(2345+i, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf")
    badServer.setErrorPages(errorHandlerMap)

    val badProps = new Properties()
    badProps.put("code", code.toString)
    badServer.addServlet(classOf[ErrorServlet], "/", badProps)
    badServer.start()

    val response = new HttpTester
    response.parse(badServer(request.generate))
    badServer.stop()
    response
  }

  def makeRequest: HttpTester = {
    val request = new HttpTester()
    request.setMethod("GET")
    request.setURI("/")
    request.setVersion("HTTP/1.0")
    request
  }

  "JettyServer" >> {
    var testServer: TestServer = null

    doBefore {
      testServer = new TestServer(7575, 0, 1, Nil, "data/keystore", "asdfasdf", "asdfasdf")
    }

    "setErrorHandler" >> {
      "not install an ErrorHandler if None is passed" >> {
        testServer.context.getErrorHandler mustBe null
      }

      "installs an ErrorHandler if Some(handler) is passed" >> {
        val handler = new ErrorHandler()
        testServer.setErrorHandler(handler)
        testServer.context.getErrorHandler mustEqual handler
      }

      "errorHandler responds to error codes" >> {

        "400" >> {
          val code = 400

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a " + code)
        }

        "500" >> {
          val code = 500

          val errorHandlerMap = Map(code -> ("You got a " + code))
          val response = makeRequestThroughErrorServlet(makeRequest, errorHandlerMap, code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a " + code)
        }

        "501" >> {
          val code = 501

          val errorHandlerMap = Map(code -> ("You got a " + code))
          val response = makeRequestThroughErrorServlet(makeRequest, errorHandlerMap, code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a " + code)
        }

        "502" >> {
          val code = 502

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a " + code)
        }

        "503" >> {
          val code = 503

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a 503")
        }

        "504" >> {
          val code = 504

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent mustNot beNull
          response.getContent must equalIgnoreSpace("You got a " + code)
        }
      }

      "doesn't respond to non-error codes" >> {

        "200" >> {
          val code = 200

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent must beNull
          response.getContent mustNot equalIgnoreSpace("You got a " + code)
        }

        "301" >> {
          val code = 301

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent must beNull
          response.getContent mustNot equalIgnoreSpace("You got a " + code)
        }

        "302" >> {
          val code = 302

          val response = makeRequestThroughErrorServlet(makeRequest, Map(code -> ("You got a " + code)), code)
          response.getStatus mustEqual code
          response.getContent must beNull
          response.getContent mustNot equalIgnoreSpace("You got a " + code)
        }
      }
    }
  }
}

