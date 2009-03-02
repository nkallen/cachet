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
    var response = null: HttpServletResponse
    var responseCapturer = null: ResponseCapturer
    var streamCapturer = null: ServletOutputStreamCapturer

    "Servlet Mutators" >> {
      doBefore{
        response = mock[HttpServletResponse]
        streamCapturer = mock[ServletOutputStreamCapturer]
        expect{allowing(streamCapturer).writeTo(response)}
        responseCapturer = new ResponseCapturer(response, streamCapturer)
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

        "writeTo(r) invokes r.addIntHeader(n, v)" >> {
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

      "Response Body" >> {
        "getWriter such that" >> {
          "getWriter.print(c) writes to streamCapturer" >> {
            expect{
              one(streamCapturer).write(a[Array[Byte]], a[Int], a[Int]) then
                      one(streamCapturer).flush
            }
            responseCapturer.getWriter.print('1')
            responseCapturer.getWriter.flush
          }
        }

        "getOutputStream" >> {
          "returns streamCapturer" >> {
            responseCapturer.getOutputStream mustEqual streamCapturer
          }
        }

        "writeTo(r) invokes streamCapture.writeTo(r)" >> {
          streamCapturer = mock[ServletOutputStreamCapturer]
          expect{one(streamCapturer).writeTo(response)}
          responseCapturer = new ResponseCapturer(response, streamCapturer)

          responseCapturer.writeTo(response)
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
        "when the age is set by setIntHeader" >> {
          responseCapturer.setIntHeader("Age", 10)
          responseCapturer.age mustEqual Some(10)
        }

        "when the age is set by setHeader" >> {
          responseCapturer.setHeader("Age", "10")
          responseCapturer.age mustEqual Some(10)
        }
      }

      "expires" >> {
        "when the expires is set by setDateHeader" >> {
          responseCapturer.setDateHeader("Expires", 10)
          responseCapturer.expires mustEqual Some(10)
        }

        "when the expires is set by setHeader" >> {

        }

      }

      "date" >> {
        "when the date is set by setDateHeader" >> {
          responseCapturer.setDateHeader("Date", 10)
          responseCapturer.date mustEqual Some(10)
        }

        "when the date is set by setHeader" >> {

        }
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

      responseCapturer.isCommitted must beFalse
      responseCapturer.getBufferSize mustEqual 0
    }
  }
}