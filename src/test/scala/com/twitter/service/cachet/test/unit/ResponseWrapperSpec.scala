package com.twitter.service.cachet.test.unit

import _root_.javax.servlet.ServletOutputStream
import com.twitter.service.cachet._
import java.io.{PrintWriter, OutputStream, ByteArrayOutputStream}
import java.lang.String
import java.util.Locale
import javax.servlet.http._
import net.sf.ehcache.constructs.web.filter.FilterServletOutputStream
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object ResponseWrapperSpec extends Specification with JMocker with ClassMocker {
  "ResponseWrapper" should {
    var response: HttpServletResponse = null
    var responseWrapper: ResponseWrapper = null

    "Servlet Mutators" >> {
      doBefore{
        response = mock[HttpServletResponse]
        responseWrapper = new ResponseWrapper(response)
      }

      "addDateHeader(x, y) such that" >> {
        val millis = System.currentTimeMillis

        "writeTo(r) invokes r.addDateHeader(x, y)" >> {
          responseWrapper.addDateHeader("Date", millis)
          expect{one(response).addDateHeader("Date", millis)}
          responseWrapper.writeTo(response)
        }
      }

      "addCookie(c) such that" >> {
        val cookie = new Cookie("key", "value")

        "writeTo(r) invokes r.addCookie(c)" >> {
          responseWrapper.addCookie(cookie)
          expect{one(response).addCookie(cookie)}
          responseWrapper.writeTo(response)
        }
      }

      "setCharacterEncoding(e) such that" >> {
        "writeTo(r) invokes r.setCharacterEncoding(e)" >> {
          responseWrapper.setCharacterEncoding("UTF-8")
          expect{one(response).setCharacterEncoding("UTF-8")}
          responseWrapper.writeTo(response)
        }
      }

      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseWrapper.addHeader(name, value)
          expect{one(response).addHeader(name, value)}
          responseWrapper.writeTo(response)
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1

        "getHeader(n) returns v" >> {
          responseWrapper.addIntHeader(name, value)
          responseWrapper.getIntHeader(name) mustEqual Some(value)
        }

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseWrapper.addIntHeader(name, value)
          expect{one(response).addIntHeader(name, value)}
          responseWrapper.writeTo(response)
        }
      }

      "sendError(sc) such that" >> {
        val sc = 200

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseWrapper.sendError(sc)
          expect{one(response).setStatus(sc)}
          responseWrapper.writeTo(response)
        }
      }

      "setStatus(sc) such that" >> {
        val sc = 200

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseWrapper.setStatus(sc)
          expect{one(response).setStatus(sc)}
          responseWrapper.writeTo(response)
        }
      }

      "setContentType(ct) such that" >> {
        val ct = "text/html"

        "writeTo(r) invokes r.setContentType(sc)" >> {
          responseWrapper.setContentType(ct)
          expect{one(response).setContentType(ct)}
          responseWrapper.writeTo(response)
        }
      }

      "setLocale(l) such that" >> {
        val l = Locale.CANADA

        "writeTo(r) invokes r.setLocale(l)" >> {
          responseWrapper.setLocale(l)
          expect{one(response).setLocale(l)}
          responseWrapper.writeTo(response)
        }
      }

      "setContentLength(l) such that" >> {
        "writeTo(r) invokes r.setContentLength(l)" >> {
          responseWrapper.setContentLength(100)
          expect{one(response).setContentLength(100)}
          responseWrapper.writeTo(response)
        }

      }

      "getWriter such that" >> {
        "writeTo(r) writes to r.getOutputStream" >> {
          val outputStream = new ByteArrayOutputStream
          val servletOutputStream = new FilterServletOutputStream(outputStream)
          expect{one(response).getOutputStream willReturn servletOutputStream}

          responseWrapper.getWriter.print(1)
          responseWrapper.writeTo(response)
          outputStream.toString
        }
      }

      "getOutputStream such that" >> {
        "writeTo(r) writes to r.getOutputStream" >> {
          val outputStream = new ByteArrayOutputStream
          val servletOutputStream = new FilterServletOutputStream(outputStream)
          expect{one(response).getOutputStream willReturn servletOutputStream}

          responseWrapper.getOutputStream.write(1)
          responseWrapper.writeTo(response)
          outputStream.toByteArray.apply(0) mustEqual 1
        }
      }
    }

    "Freshness Information" >> {
      "maxAge" >> {
        "when there is a max-age control" >> {
          responseWrapper.setHeader("Cache-Control", "max-age=100")
          responseWrapper.maxAge mustEqual Some(100)
        }

        "when there is a s-maxage control" >> {
          responseWrapper.setHeader("Cache-Control", "s-maxage=100")
          responseWrapper.maxAge mustEqual Some(100)
        }

        "when both a max-age and s-maxage are present" >> {
          "returns s-maxage" >> {
            responseWrapper.setHeader("Cache-Control", "s-maxage=1, max-age=2")
            responseWrapper.maxAge mustEqual Some(1)
          }
        }
      }

      "age" >> {
        true
      }

      "expires" >> {
        true
      }

      "date" >> {
        true
      }
    }

    "not delegate buffering commands to the response" >> {
      expect{
        never(response).flushBuffer
        never(response).reset
        never(response).resetBuffer
        never(response).setBufferSize(an[Int])
      }

      responseWrapper.flushBuffer
      responseWrapper.reset
      responseWrapper.resetBuffer
      responseWrapper.setBufferSize(100)

      responseWrapper.isCommitted mustBe false
      responseWrapper.getBufferSize mustEqual 0
    }
  }
}