package com.twitter.service.cachet.test.unit

import _root_.com.twitter.service.cache.client.ForwardRequest
import client.{HttpRequest, HttpClient}
import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ForwardRequestSpec extends Specification with JMocker with ClassMocker {
  var forwardRequest = null: ForwardRequest
  var httpRequest = mock[HttpRequest]
  var servletRequest = null: HttpServletRequest
  var servletResponse = null: HttpServletResponse
  var httpClient = null: HttpClient

  "ForwardRequest" should {
    doBefore{
      httpClient = mock[HttpClient]
      httpRequest = mock[HttpRequest]
      servletRequest = mock[HttpServletRequest]
      servletResponse = mock[HttpServletResponse]

      forwardRequest = new ForwardRequest(httpClient)
    }

    "apply" >> {
      "sets the request's method, url, headers, etc. on the client, and invokes the client" >> {
        expect{
          one(httpClient).newRequest willReturn (httpRequest)
          one(httpRequest).execute("localhost", 3000, servletRequest, servletResponse)
        }
        forwardRequest(servletRequest, servletResponse)
      }
    }
  }
}