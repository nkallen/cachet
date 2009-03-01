package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import java.io.PrintWriter
import java.lang.String
import java.util.Locale
import javax.servlet.http._

import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ResponseBufferSpec extends Specification with JMocker {
  "ResponseBuffer" should {
    var response: HttpServletResponse = null
    var responseBuffer: ResponseBuffer = null

    "have accessors" >> {
      doBefore{
        response = mock[HttpServletResponse]
        responseBuffer = new ResponseBuffer(response)
      }

      "addDateHeader(x, y) such that" >> {
        val millis = System.currentTimeMillis

        "getDateHeader(x) returns y" >> {
          responseBuffer.addDateHeader("Date", millis)
          responseBuffer.getDateHeader("Date") mustEqual Some(millis)
        }

        "writeTo(r) invokes r.addDateHeader(x, y)" >> {
          responseBuffer.addDateHeader("Date", millis)
          expect{one(response).addDateHeader("Date", millis)}
          responseBuffer.writeTo(response)
        }
      }

      "addCookie(c) such that" >> {
        val cookie = new Cookie("key", "value")

        "cookies" >> {
          responseBuffer.addCookie(cookie)
          responseBuffer.getCookies.contains(cookie) mustBe true
        }
      }

      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"

        "getHeader(n) returns v" >> {
          responseBuffer.addHeader(name, value)
          responseBuffer.getHeader(name) mustEqual Some(value)
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1

        "getHeader(n) returns v" >> {
          responseBuffer.addIntHeader(name, value)
          responseBuffer.getIntHeader(name) mustEqual Some(value)
        }
      }

      "sendError(...) such that" >> {
        "sendError(sc)" >> {
          val sc = 200

          "getStatus() returns sc" >> {
            responseBuffer.sendError(sc)
            responseBuffer.getStatus mustEqual sc
          }
        }

        "sendError(sc)" >> {
          val sc = 200

          "getStatus() returns sc" >> {
            responseBuffer.sendError(sc)
            responseBuffer.getStatus mustEqual sc
          }
        }
      }

      "setContentType(ct) such that" >> {
        val ct = "text/html"

        "getContentType returns ct" >> {
          responseBuffer.setContentType(ct)
          responseBuffer.getContentType mustEqual ct
        }
      }

      "setLocale(l) such that" >> {
        val l = Locale.CANADA

        "getLocale returns l" >> {
          responseBuffer.setLocale(l)
          responseBuffer.getLocale mustEqual l
        }
      }

      "setContentLength(l) such that" >> {
        "getContentLength returns l" >> {
          responseBuffer.setContentLength(100)
          responseBuffer.getContentLength mustEqual 100
        }
      }

      "getWriter such that" >> {

      }

      "getOutputStream such that" >> {

      }
    }
  }
}