package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import org.mortbay.jetty.client.{HttpClient, HttpExchange}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ProxyRequestSpec extends Specification with JMocker with ClassMocker {
  var exchange: HttpExchange = null
  var proxyRequest: ProxyRequest = null
  var request: FakeHttpServletRequest = null
  var response: HttpServletResponse = null
  var client: HttpClient = null

  "ProxyRequest" should {
    doBefore{
      exchange = mock[HttpExchange]
      client = mock[HttpClient]
      request = new FakeHttpServletRequest
      response = new FakeHttpServletResponse

      proxyRequest = new ProxyRequest(client, (blah, blar) => exchange)
    }

    "when request isInitial" >> {
      "sets the request's method, url, headers, etc. on the exchange, and invokes the client" >> {
        request.method = "PUT"
        request.path = "/path"
        request.isInitial = true
        request.setHeader("foo", "bar")

        expect{one(exchange).setMethod("PUT")}
        expect{one(exchange).setURL("http://localhost:3000" + request.getRequestURI)}
        expect{one(exchange).setRequestContentSource(request.getInputStream)}
        expect{one(exchange).setRequestHeader("foo", "bar")}
        expect{one(client).send(exchange)}

        proxyRequest(request, response)
      }
    }

    "when the request !isInitial" >> {
      doBefore {
        request.isInitial = false
      }

      "does nothing" >> {
        expect{never(client).send(exchange)}
        proxyRequest(request, response)
      }
    }
  }
}