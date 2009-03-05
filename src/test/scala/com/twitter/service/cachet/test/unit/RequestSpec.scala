package com.twitter.service.cachet.test.unit

import _root_.com.twitter.service.cache.client.ForwardRequest
import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.http.client.HttpClient
import org.apache.http.HttpHost
import org.apache.http.message.BasicHttpRequest
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ForwardRequestSpec extends Specification with JMocker with ClassMocker {
  var forwardRequest = null: ForwardRequest
  var request = null: FakeHttpServletRequest
  var response = null: HttpServletResponse
  var client = null: HttpClient

  "ForwardRequest" should {
    doBefore{
      client = mock[HttpClient]
      request = new FakeHttpServletRequest
      response = mock[HttpServletResponse]

      forwardRequest = new ForwardRequest(client)
    }

    "apply" >> {
      "sets the request's method, url, headers, etc. on the exchange, and invokes the client" >> {
        request.method = "PUT"
        request.path = "/path"
        request.isInitial = true
        request.setHeader("foo", "bar")

        expect{one(client).execute(a[HttpHost], a[BasicHttpRequest])}
        forwardRequest(request, response)
      }
    }
  }
}