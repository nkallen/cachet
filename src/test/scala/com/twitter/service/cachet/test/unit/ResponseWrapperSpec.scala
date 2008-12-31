package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http._

import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ResponseWrapperSpec extends Specification with JMocker {
  "ResponseWrapper" should {
    var response: HttpServletResponse = null
    var responseWrapper: ResponseWrapper = null

    "have accessors" >> {
      doBefore {
        response = mock(classOf[HttpServletResponse])
        responseWrapper = new ResponseWrapper(response)
      }

      "addDateHeader(x, y) such that" >> {
        val millis = System.currentTimeMillis

        "it delegates to the response" +
        "getDateHeader(x) returns y" >> {
          expect { one(response).addDateHeader("Date", millis) }
          responseWrapper.addDateHeader("Date", millis)
          responseWrapper.getDateHeader("Date") must be_==(Some(millis))
        }
      }

      "addCookie(c) such that" >> {
        val cookie = new Cookie("key", "value")

        "it delegates to the response" +
        "cookies" >> {
          expect { one(response).addCookie(cookie) }
          responseWrapper.addCookie(cookie)
          responseWrapper.getCookies.contains(cookie) must be_==(true)
        }
      }
    
      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"
      
        "it delegates to the response" +
        "getHeader(n) returns v" >> {
          expect { one(response).addHeader(name, value) }
          responseWrapper.addHeader(name, value)
          responseWrapper.getHeader(name) must be_==(Some(value))
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1
      
        "it delegates to the response" +
        "getHeader(n) returns v" >> {
          expect { one(response).addIntHeader(name, value) }
          responseWrapper.addIntHeader(name, value)
          responseWrapper.getIntHeader(name) must be_==(Some(value))
        }
      }

      "sendError(...) such that" >> {
        "sendError(sc)" >> {
          val sc = 200

          "delegates to the response" +
          "getStatus() returns sc" >> {
            expect { one(response).sendError(sc) }
            responseWrapper.sendError(sc)
            responseWrapper.getStatus must be_==(sc)
          }
        }

        "sendError(sc)" >> {
          val sc = 200

          "delegates to the response" +
          "getStatus() returns sc" >> {
            expect { one(response).sendError(sc) }
            responseWrapper.sendError(sc)
            responseWrapper.getStatus must be_==(sc)
          }
        }
      }
    }
  }
}