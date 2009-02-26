package com.twitter.service.cachet.test.unit


import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.twitter.service.cachet._
import mock.FakeHttpServletResponse
import org.specs.mock.{ClassMocker, JMocker}
import org.specs.Specification

object CopyExchangeSpec extends Specification with JMocker with ClassMocker {
  var request: HttpServletRequest = null
  var response: HttpServletResponse = null

  "CopyExchange" should {
    doBefore {
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]
    }

    "initialize & onResponseComplete" >> {
      "suspends and resumes the request" >> {
        expect { one(request).suspend() }
        expect { one(request).resume() }
        val exchange = new CopyExchange(request, response)
        exchange.onResponseComplete
      }
    }

    "while performing the request" >> {
      var exchange: CopyExchange = null

      doBefore {
//        expect { one(request).suspend() }
//        exchange = new CopyExchange(request, response)
      }

      "onResponseHeader" >> {
        "copies the header to the response" >> {

        }
      }

      "onResponseContent" >> {
        "copies the content to the response" >> {

        }
      }

      "onResponseStatus" >> {
        "copies the status to the response" >> {

        }
      }
    }
  }
}