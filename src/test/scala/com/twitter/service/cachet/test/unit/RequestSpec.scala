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
  var request: HttpServletRequest = null
  var response: HttpServletResponse = null
  var client: HttpClient = null

  "ProxyRequest" should {
    doBefore {
      exchange = mock(classOf[HttpExchange])
      request = mock(classOf[HttpServletRequest])
      response = new FakeHttpServletResponse
      client = mock(classOf[HttpClient])

      proxyRequest = new ProxyRequest(client, (blah, blar) => exchange)      
    }

    "when request isInitial" >> {
      val uri = "/uri"
      val method = "PUT"

      "sets the request's method, url on the exchange, and invokes the client" >> {
        expect { one(request).isInitial willReturn(true) }
        expect { one(request).getRequestURI willReturn(uri) }
        expect { one(request).getMethod willReturn(method) }

        expect { one(exchange).setMethod(method) }
        expect { one(exchange).setURL("http://localhost:3000" + uri) }
        expect { one(client).send(exchange) }
        proxyRequest(request, response)
      }
    }

    "when the request !isInitial" >> {
      "does nothing" >> {
        expect { one(request).isInitial willReturn(false) }

        proxyRequest(request, response)
      }
    }
  }
}