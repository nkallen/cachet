package com.twitter.service.cachet.test.unit

import _root_.com.twitter.service.cache.client.ForwardRequest
import client.HttpClient
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
  var forwardRequest = null: HttpClient
  var request = null: FakeHttpServletRequest
  var response = null: HttpServletResponse
  var httpClient = null: HttpClient

  "ForwardRequest" should {
    doBefore{
      httpClient = mock[HttpClient]
      request = new FakeHttpServletRequest
      response = mock[HttpServletResponse]

      forwardRequest = new ForwardRequest(httpClient)
    }

    "apply" >> {
      "sets the request's method, url, headers, etc. on the client, and invokes the client" >> {
        request.method = "PUT"
        request.path = "/path"
        request.isInitial = true
        request.setHeader("foo", "bar")

        expect{
          one(httpClient).host = "localhost"
          one(httpClient).port = 3000
          one(httpClient).scheme = "http"
          one(httpClient).method = "PUT"
          one(httpClient).uri = "/path"
          one(httpClient).queryString = null
          one(httpClient).addHeader("foo", "bar")
          one(httpClient).performAndWriteTo(response)
        }
        forwardRequest(request, response)
      }
    }
  }
}