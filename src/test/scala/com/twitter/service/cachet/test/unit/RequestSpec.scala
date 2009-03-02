package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.mortbay.jetty.client.{HttpClient, HttpExchange}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ClientRequestSpec extends Specification with JMocker with ClassMocker {
  var exchange = null: HttpExchange
  var clientRequest = null: ClientRequest
  var request = null: FakeHttpServletRequest
  var response = null: HttpServletResponse
  var client = null: HttpClient

  "ClientRequest" should {
    doBefore{
      exchange = mock[HttpExchange]
      client = mock[HttpClient]
      request = new FakeHttpServletRequest
      response = mock[HttpServletResponse]

      clientRequest = new ClientRequest(client, (blah, blar) => exchange)
    }

    "when request isInitial" >> {
      "sets the request's method, url, headers, etc. on the exchange, and invokes the client" >> {
        request.method = "PUT"
        request.path = "/path"
        request.isInitial = true
        request.setHeader("foo", "bar")

        expect{
          one(exchange).setMethod("PUT")
          one(exchange).setURL("http://localhost:3000" + request.getRequestURI)
          one(exchange).setRequestContentSource(request.getInputStream)
          one(exchange).setRequestHeader("foo", "bar")
          one(client).send(exchange)
        }
        clientRequest(request, response)
      }
    }

    "when the request !isInitial" >> {
      doBefore{
        request.isInitial = false
      }

      "does nothing" >> {
        skip("suspendable requests temporarily disabled")
        expect{never(client).send(exchange)}
        clientRequest(request, response)
      }
    }
  }
}