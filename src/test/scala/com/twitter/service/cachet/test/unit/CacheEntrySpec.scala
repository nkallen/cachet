package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http._

import org.jmock.core.Stub
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object CacheEntrySpec extends Specification with JMocker with ClassMocker {
  "CacheEntry" should {
    var responseWrapper: ResponseWrapper = null
    var cacheEntry: CacheEntry = null
    var response: HttpServletResponse = null

    "implement RFC 2616" >> {
      doBefore{
        response = new FakeHttpServletResponse
        responseWrapper = mock[ResponseWrapper]
        cacheEntry = new CacheEntry(responseWrapper)
        cacheEntry.noteResponseTime()
      }

      "age calculations" >> {
        "dateValue" >> {
          "when there is a Date header" >> {
            "returns the value of the header" >> {
              val millis = System.currentTimeMillis
              expect{allowing(responseWrapper).date willReturn Some(millis)}
              cacheEntry.dateValue mustEqual millis
            }
          }

          "when there is no Date header" >> {
            "returns the response time" >> {
              expect{allowing(responseWrapper).date willReturn None}
              cacheEntry.dateValue mustEqual cacheEntry.responseTime
            }
          }
        }

        //        "apparentAge" >> {
        //          "when dateValue <= responseTime" >> {
        //            "returns responseTime - dateValue" >> {
        //              val delta = 10
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime - delta)
        //              cacheEntry.apparentAge mustEqual delta
        //            }
        //          }
        //
        //          "when dateValue > responseTime" >> {
        //            "returns 0" >> {
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime + 10)
        //              cacheEntry.apparentAge mustEqual 0
        //            }
        //          }
        //        }
        //
        //        "correctedReceivedAge" >> {
        //          "when apparentAge > ageValue" >> {
        //            "returns apparentAge" >> {
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime + 1)
        //              responseWrapper.setIntHeader("Age", 0)
        //              cacheEntry.correctedReceivedAge mustEqual cacheEntry.apparentAge
        //            }
        //          }
        //
        //          "when apparentAge < ageValue" >> {
        //            "returns ageValue" >> {
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //              responseWrapper.setIntHeader("Age", 10)
        //              cacheEntry.correctedReceivedAge mustEqual responseWrapper.getIntHeader("Age").get
        //            }
        //          }
        //
        //          "when no ageValue" >> {
        //            "returns apparentAge" >> {
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //              cacheEntry.correctedReceivedAge mustEqual cacheEntry.apparentAge
        //            }
        //          }
        //        }
        //
        //        "responseDelay" >> {
        //          "returning responseTime - requestTime" >> {
        //            cacheEntry.responseDelay mustEqual cacheEntry.responseTime - cacheEntry.requestTime
        //          }
        //        }
        //
        //        "correctedInitialAge" >> {
        //          "returning correctedReceivedAge + responseDelay" >> {
        //            responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //            responseWrapper.setIntHeader("Age", 10)
        //            cacheEntry.correctedInitialAge mustEqual (10 + cacheEntry.responseDelay)
        //          }
        //        }
        //
        //        "residentTime" >> {
        //          "returning now - responseTime" >> {
        //            cacheEntry.residentTime mustEqual (System.currentTimeMillis - cacheEntry.responseTime)
        //          }
        //        }
        //
        //        "currentAge" >> {
        //          "returning correctedInitialAge + residentTime" >> {
        //            responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //            responseWrapper.setIntHeader("Age", 10)
        //            cacheEntry.currentAge mustEqual (cacheEntry.correctedInitialAge + cacheEntry.residentTime)
        //          }
        //        }
        //
        //        "maxAgeValue" >> {
        //          "when there is a max-age control" >> {
        //            responseWrapper.setHeader("Cache-Control", "max-age=100")
        //            cacheEntry.maxAgeValue mustEqual Some(100)
        //          }
        //
        //          "when there is a s-maxage control" >> {
        //            responseWrapper.setHeader("Cache-Control", "s-maxage=100")
        //            cacheEntry.maxAgeValue mustEqual Some(100)
        //          }
        //
        //          "when both a max-age and s-maxage are present" >> {
        //            "returns s-maxage" >> {
        //              responseWrapper.setHeader("Cache-Control", "s-maxage=1, max-age=2")
        //              cacheEntry.maxAgeValue mustEqual Some(1)
        //            }
        //          }
        //        }
        //      }
        //
        //      "expiration calculations" >> {
        //        "when there is a max-age directive" >> {
        //          "freshnessLifetime" >> {
        //            "returns maxAgeValue" >> {
        //              responseWrapper.setHeader("Cache-Control", "s-maxage=1")
        //              cacheEntry.freshnessLifetime mustEqual Some(1)
        //            }
        //          }
        //        }
        //
        //        "when there is no max-age directive" >> {
        //          "when there is an Expires header" >> {
        //            "returns expiresValue - dateValue" >> {
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //              responseWrapper.setDateHeader("Expires", cacheEntry.responseTime + 10)
        //              cacheEntry.freshnessLifetime mustEqual Some(10)
        //            }
        //          }
        //        }
        //
        //        "isFresh" >> {
        //          "when freshnessLifetime >= currentAge" >> {
        //            "returns true" >> {
        //              responseWrapper.setHeader("Cache-Control", "s-maxage=100")
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime)
        //              cacheEntry.isFresh must beTrue
        //            }
        //          }
        //
        //          "when freshnessLifetime < currentAge" >> {
        //            "returns false" >> {
        //              responseWrapper.setHeader("Cache-Control", "s-maxage=1")
        //              responseWrapper.setDateHeader("Date", cacheEntry.responseTime - 100)
        //              cacheEntry.isFresh must beFalse
        //            }
        //          }
        //
        //          "when there is no freshnessLifetime" >> {
        //            "returns false" >> {
        //              cacheEntry.isFresh must beFalse
        //            }
        //          }
        //        }
        //      }
      }
      //
      //    "writeTo" >> {
      //      "delegates to the responseWrapper" >> {
      //        val response = new FakeHttpServletResponse
      //        //        expect{one(responseWrapper).writeTo(response)}
      //        cacheEntry.writeTo(response)
      //      }
    }
  }
}