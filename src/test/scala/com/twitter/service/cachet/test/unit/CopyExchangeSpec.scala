package com.twitter.service.cachet.test.unit


import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import _root_.javax.servlet.ServletOutputStream
import com.twitter.service.cachet._
import mock.{FakeHttpServletResponse, FakeHttpServletRequest}
import org.mortbay.io.{ByteArrayBuffer, Buffer}
import org.specs.mock.{ClassMocker, JMocker}
import org.specs.Specification

object CopyExchangeSpec extends Specification with JMocker with ClassMocker {
  var exchange: CopyExchange = null
  var request: HttpServletRequest = null
  var response: HttpServletResponse = null

  "CopyExchange" should {
    "initialize & onResponseComplete" >> {
      "suspends and resumes the request" >> {
        request = mock[HttpServletRequest]
        expect{
          one(request).suspend() then
                  one(request).resume()
        }
        exchange = new CopyExchange(request, null: HttpServletResponse)
        exchange.onResponseComplete
      }
    }

    "while performing the request" >> {

      doBefore{
        request = new FakeHttpServletRequest
        response = mock[HttpServletResponse]

        exchange = new CopyExchange(request, response)
      }

      "onResponseHeader" >> {
        "copies the header to the response" >> {
          val name = new ByteArrayBuffer("name")
          val value = new ByteArrayBuffer("value")
          expect{one(response).setHeader(name.toString, value.toString)}
          exchange.onResponseHeader(name, value)
        }
      }

      "onResponseContent" >> {
        "copies the content to the response" >> {
          val content = mock[Buffer]
          val outputStream = mock[ServletOutputStream]
          expect{allowing(response).getOutputStream willReturn (outputStream)}
          expect{one(content).writeTo(outputStream)}
          exchange.onResponseContent(content)
        }
      }

      "onResponseStatus" >> {
        "copies the status to the response" >> {
          expect{one(response).setStatus(200)}
          exchange.onResponseStatus(null: Buffer, 200, null: Buffer)
        }
      }
    }
  }
}