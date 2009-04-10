package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet.proxy.client.{HttpClient, ForwardRequest, RequestSpecification, ResponseWrapper}
import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ForwardRequestSpec extends Specification with JMocker with ClassMocker {
  var forwardRequest = null: ForwardRequest
  var servletRequest = null: FakeHttpServletRequest
  var servletResponse = null: HttpServletResponse
  var httpClient = null: HttpClient

  "ForwardRequest" should {
    doBefore{
      httpClient = mock[HttpClient]
      servletRequest = new FakeHttpServletRequest
      servletResponse = mock[HttpServletResponse]

      forwardRequest = new ForwardRequest(httpClient, "localhost", 80)
    }

    "apply" >> {
      "sets the request's method, url, headers, etc. on the client, and invokes the client" >> {
        expect{
          one(httpClient).apply(a[String], an[Int], a[RequestSpecification], a[ResponseWrapper])
          one(servletResponse).addHeader("Via", "Cachet/0.10")
        }
        forwardRequest(servletRequest, servletResponse)
      }
    }

    "ResponseWrapper" >> {
      "addHeader" >> {
        "propagates normal headers" >> {
          val responseWrapper = new ResponseWrapper(servletResponse)
          expect{never(servletResponse).addHeader(a[String], a[String])}
          responseWrapper.addHeader("Proxy-Connection", "bar")
        }

        "does not propagate hop-by-hop headers" >> {
          val responseWrapper = new ResponseWrapper(servletResponse)
          expect{one(servletResponse).addHeader("foo", "bar")}
          responseWrapper.addHeader("foo", "bar")
        }
      }
    }

    "RequestSpecification" >> {
      "getHeaders" >> {
        "propagates normal headers" >> {
          val requestWrapper = new RequestSpecification(servletRequest)
          servletRequest.setHeader("foo", "bar")
          requestWrapper.headers.contains(("foo", "bar")) must beTrue
        }

        "does not propagate hop-by-hop headers" >> {
          val requestWrapper = new RequestSpecification(servletRequest)
          servletRequest.setHeader("Proxy-Connection", "bar")
          requestWrapper.headers.contains("Proxy-Connection") must beFalse
        }

        "X-Forwarded-For" >> {
          "when the request doesn't have an X-Forwarded-For header" >> {
            "sets the X-Forwaded-For header" >> {
              val requestWrapper = new RequestSpecification(servletRequest)
              servletRequest.remoteAddr = "1.1.1.1"
              requestWrapper.headers.contains(("X-Forwarded-For", "1.1.1.1")) must beTrue
            }
          }

          "when the request has an X-Forwarded-For header" >> {
            "appends to the X-Forwarded-For header" >> {
              val requestWrapper = new RequestSpecification(servletRequest)
              servletRequest.remoteAddr = "2.2.2.2"
              servletRequest.setHeader("X-Forwarded-For", "1.1.1.1")
              requestWrapper.headers.contains(("X-Forwarded-For", "1.1.1.1, 2.2.2.2")) must beTrue
              requestWrapper.headers.contains(("X-Forwarded-For", "1.1.1.1")) must beFalse
            }
          }
        }
      }
    }
  }
}
