package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import java.lang.String
import java.util.Locale
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
        response = mock[HttpServletResponse]
        responseWrapper = new ResponseWrapper(response)
      }

      "addDateHeader(x, y) such that" >> {
        val millis = System.currentTimeMillis

        "getDateHeader(x) returns y" >> {
          responseWrapper.addDateHeader("Date", millis)
          responseWrapper.getDateHeader("Date") mustEqual(Some(millis))
        }
      }

      "addCookie(c) such that" >> {
        val cookie = new Cookie("key", "value")

        "cookies" >> {
          responseWrapper.addCookie(cookie)
          responseWrapper.getCookies.contains(cookie) mustEqual(true)
        }
      }
    
      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"
      
        "getHeader(n) returns v" >> {
          responseWrapper.addHeader(name, value)
          responseWrapper.getHeader(name) mustEqual(Some(value))
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1
      
        "getHeader(n) returns v" >> {
          responseWrapper.addIntHeader(name, value)
          responseWrapper.getIntHeader(name) mustEqual(Some(value))
        }
      }

      "sendError(...) such that" >> {
        "sendError(sc)" >> {
          val sc = 200

          "getStatus() returns sc" >> {
            responseWrapper.sendError(sc)
            responseWrapper.getStatus mustEqual(sc)
          }
        }

        "sendError(sc)" >> {
          val sc = 200

          "getStatus() returns sc" >> {
            responseWrapper.sendError(sc)
            responseWrapper.getStatus mustEqual(sc)
          }
        }
      }
    }

    "setContentType(ct) such that" >> {
      val ct = "text/html"

      "getContentType returns ct" >> {
        responseWrapper.setContentType(ct)
        responseWrapper.getContentType mustEqual(ct)
      }
    }

    "setLocale(l) such that" >> {
      val l = Locale.CANADA

      "getLocale(l) returns l" >> {
        responseWrapper.setLocale(l)
        responseWrapper.getLocale mustEqual(l)
      }
    }    
  }
}