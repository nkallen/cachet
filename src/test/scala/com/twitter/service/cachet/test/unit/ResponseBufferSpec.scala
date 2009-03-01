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
        responseBuffer = new ResponseBuffer
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

        "writeTo(r) invokes r.addCookie(c)" >> {
          responseBuffer.addCookie(cookie)
          expect{one(response).addCookie(cookie)}
          responseBuffer.writeTo(response)
        }
      }

      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"

        "getHeader(n) returns v" >> {
          responseBuffer.addHeader(name, value)
          responseBuffer.getHeader(name) mustEqual Some(value)
        }

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseBuffer.addHeader(name, value)
          expect{one(response).addHeader(name, value)}
          responseBuffer.writeTo(response)
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1

        "getHeader(n) returns v" >> {
          responseBuffer.addIntHeader(name, value)
          responseBuffer.getIntHeader(name) mustEqual Some(value)
        }

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseBuffer.addIntHeader(name, value)
          expect{one(response).addIntHeader(name, value)}
          responseBuffer.writeTo(response)
        }
      }

      "sendError(sc) such that" >> {
        val sc = 200

        "getStatus() returns sc" >> {
          responseBuffer.sendError(sc)
          responseBuffer.getStatus mustEqual sc
        }

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseBuffer.sendError(sc)
          expect{one(response).setStatus(sc)}
          responseBuffer.writeTo(response)
        }
      }

      "setStatus(sc) such that" >> {
        val sc = 200

        "getStatus returns sc" >> {
          responseBuffer.setStatus(sc)
          responseBuffer.getStatus mustEqual sc
        }

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseBuffer.setStatus(sc)
          expect{one(response).setStatus(sc)}
          responseBuffer.writeTo(response)
        }
      }

      "setContentType(ct) such that" >> {
        val ct = "text/html"

        "writeTo(r) invokes r.setContentType(sc)" >> {
          responseBuffer.setContentType(ct)
          expect{one(response).setContentType(ct)}
          responseBuffer.writeTo(response)
        }
      }

      "setLocale(l) such that" >> {
        val l = Locale.CANADA

        "getLocale returns l" >> {
          responseBuffer.setLocale(l)
          responseBuffer.getLocale mustEqual l
        }

        "writeTo(r) invokes r.setLocale(l)" >> {
          responseBuffer.setLocale(l)
          expect{one(response).setLocale(l)}
          responseBuffer.writeTo(response)
        }
      }

      "setContentLength(l) such that" >> {
        "writeTo(r) invokes r.setContentLength(l)" >> {
          responseBuffer.setContentLength(100)
          expect{one(response).setContentLength(100)}
          responseBuffer.writeTo(response)
        }

      }

      "getWriter such that" >> {

      }

      "getOutputStream such that" >> {

      }
    }
  }
}