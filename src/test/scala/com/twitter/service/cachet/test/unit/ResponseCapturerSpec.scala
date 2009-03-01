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

object ResponseCapturerSpec extends Specification with JMocker with ClassMocker {
  "ResponseCapturer" should {
    var response: HttpServletResponse = null
    var responseCapturer: ResponseCapturer = null

    "Servlet Mutators" >> {
      doBefore{
        response = mock[HttpServletResponse]
        responseCapturer = new ResponseCapturer(response)
      }

      "addDateHeader(x, y) such that" >> {
        val millis = System.currentTimeMillis

        "writeTo(r) invokes r.addDateHeader(x, y)" >> {
          responseCapturer.addDateHeader("Date", millis)
          expect{one(response).addDateHeader("Date", millis)}
          responseCapturer.writeTo(response)
        }
      }

      "addCookie(c) such that" >> {
        val cookie = new Cookie("key", "value")

        "writeTo(r) invokes r.addCookie(c)" >> {
          responseCapturer.addCookie(cookie)
          expect{one(response).addCookie(cookie)}
          responseCapturer.writeTo(response)
        }
      }

      "setCharacterEncoding(e) such that" >> {
        "writeTo(r) invokes r.setCharacterEncoding(e)" >> {
          responseCapturer.setCharacterEncoding("UTF-8")
          expect{one(response).setCharacterEncoding("UTF-8")}
          responseCapturer.writeTo(response)
        }
      }

      "addHeader(n, v) such that" >> {
        val name = "name"
        val value = "value"

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseCapturer.addHeader(name, value)
          expect{one(response).addHeader(name, value)}
          responseCapturer.writeTo(response)
        }
      }

      "addIntHeader(n, i) such that" >> {
        val name = "name"
        val value = 1

        "getHeader(n) returns v" >> {
          responseCapturer.addIntHeader(name, value)
          responseCapturer.getIntHeader(name) mustEqual Some(value)
        }

        "writeTo(r) invokes r.addHeader(n, v)" >> {
          responseCapturer.addIntHeader(name, value)
          expect{one(response).addIntHeader(name, value)}
          responseCapturer.writeTo(response)
        }
      }

      "sendError(sc) such that" >> {
        val sc = 200

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseCapturer.sendError(sc)
          expect{one(response).setStatus(sc)}
          responseCapturer.writeTo(response)
        }
      }

      "setStatus(sc) such that" >> {
        val sc = 200

        "writeTo(r) invokes r.setStatus(sc)" >> {
          responseCapturer.setStatus(sc)
          expect{one(response).setStatus(sc)}
          responseCapturer.writeTo(response)
        }
      }

      "setContentType(ct) such that" >> {
        val ct = "text/html"

        "writeTo(r) invokes r.setContentType(sc)" >> {
          responseCapturer.setContentType(ct)
          expect{one(response).setContentType(ct)}
          responseCapturer.writeTo(response)
        }
      }

      "setLocale(l) such that" >> {
        val l = Locale.CANADA

        "writeTo(r) invokes r.setLocale(l)" >> {
          responseCapturer.setLocale(l)
          expect{one(response).setLocale(l)}
          responseCapturer.writeTo(response)
        }
      }

      "setContentLength(l) such that" >> {
        "writeTo(r) invokes r.setContentLength(l)" >> {
          responseCapturer.setContentLength(100)
          expect{one(response).setContentLength(100)}
          responseCapturer.writeTo(response)
        }

      }

      "getWriter such that" >> {
        "writeTo(r) writes to r.getOutputStream" >> {
          val outputStream = new ByteArrayOutputStream
          val servletOutputStream = new FilterServletOutputStream(outputStream)
          expect{one(response).getOutputStream willReturn servletOutputStream}

          responseCapturer.getWriter.print(1)
          responseCapturer.writeTo(response)
          outputStream.toString
        }
      }

      "getOutputStream such that" >> {
        "writeTo(r) writes to r.getOutputStream" >> {
          val outputStream = new ByteArrayOutputStream
          val servletOutputStream = new FilterServletOutputStream(outputStream)
          expect{one(response).getOutputStream willReturn servletOutputStream}

          responseCapturer.getOutputStream.write(1)
          responseCapturer.writeTo(response)
          outputStream.toByteArray.apply(0) mustEqual 1
        }
      }
    }

    "Freshness Information" >> {
      "maxAge" >> {
        "when there is a max-age control" >> {
          responseCapturer.setHeader("Cache-Control", "max-age=100")
          responseCapturer.maxAge mustEqual Some(100)
        }

        "when there is a s-maxage control" >> {
          responseCapturer.setHeader("Cache-Control", "s-maxage=100")
          responseCapturer.maxAge mustEqual Some(100)
        }

        "when both a max-age and s-maxage are present" >> {
          "returns s-maxage" >> {
            responseCapturer.setHeader("Cache-Control", "s-maxage=1, max-age=2")
            responseCapturer.maxAge mustEqual Some(1)
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

      responseCapturer.flushBuffer
      responseCapturer.reset
      responseCapturer.resetBuffer
      responseCapturer.setBufferSize(100)

      responseCapturer.isCommitted mustBe false
      responseCapturer.getBufferSize mustEqual 0
    }
  }
}