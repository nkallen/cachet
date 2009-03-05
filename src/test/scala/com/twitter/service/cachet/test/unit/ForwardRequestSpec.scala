package com.twitter.service.cachet.test.unit

import _root_.com.twitter.service.cache.client.ForwardRequest
import _root_.com.twitter.service.cache.client.RequestWrapper
import _root_.com.twitter.service.cache.client.ResponseWrapper
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
  var servletRequest = null: FakeHttpServletRequest
  var servletResponse = null: HttpServletResponse
  var httpClient = null: HttpClient

  "ForwardRequest" should {
    doBefore{
      httpClient = mock[HttpClient]
      httpRequest = mock[HttpRequest]
      servletRequest = new FakeHttpServletRequest
      servletResponse = mock[HttpServletResponse]

      forwardRequest = new ForwardRequest(httpClient)
    }

    "apply" >> {
      "sets the request's method, url, headers, etc. on the client, and invokes the client" >> {
        expect{
          one(httpClient).newRequest willReturn httpRequest
          one(httpRequest).execute(a[String], an[Int], a[RequestWrapper], a[ResponseWrapper])
          one(servletResponse).addHeader("Via", "NProxy")
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

    "RequestWrapper" >> {
      "getHeaders" >> {
        "propagates normal headers" >> {
          val requestWrapper = new RequestWrapper(servletRequest)
          servletRequest.setHeader("foo", "bar")
          requestWrapper.getHeaders("foo").nextElement() mustEqual "bar"
        }

        "does not propagate hop-by-hop headers" >> {
          val requestWrapper = new RequestWrapper(servletRequest)
          servletRequest.setHeader("Proxy-Connection", "bar")
          requestWrapper.getHeaders("Proxy-Connection").hasMoreElements must beFalse
        }
      }
    }
  }
}