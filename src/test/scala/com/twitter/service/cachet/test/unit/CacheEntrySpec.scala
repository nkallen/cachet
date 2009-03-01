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
        cacheEntry.noteResponseTime() // FIXME - this feels wrong?
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

        "apparentAge" >> {
          "when dateValue <= responseTime" >> {
            "returns responseTime - dateValue" >> {
              expect{allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime - 10)}
              cacheEntry.apparentAge mustEqual 10
            }
          }

          "when dateValue > responseTime" >> {
            "returns 0" >> {
              expect{allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime + 10)}
              cacheEntry.apparentAge mustEqual 0
            }
          }
        }

        "correctedReceivedAge" >> {
          "when apparentAge > ageValue" >> {
            "returns apparentAge" >> {
              expect{
                allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime + 1)
                allowing(responseWrapper).age willReturn Some(0.toLong)
              }
              cacheEntry.correctedReceivedAge mustEqual cacheEntry.apparentAge
            }
          }

          "when apparentAge < ageValue" >> {
            "returns ageValue" >> {
              expect{
                allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime)
                allowing(responseWrapper).age willReturn Some(1.toLong)
              }
              cacheEntry.correctedReceivedAge mustEqual 1
            }
          }

          "when no ageValue" >> {
            "returns apparentAge" >> {
              expect{
                allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime)
                allowing(responseWrapper).age willReturn None
              }
              cacheEntry.correctedReceivedAge mustEqual cacheEntry.apparentAge
            }
          }
        }

        "responseDelay" >> {
          "returning responseTime - requestTime" >> {
            cacheEntry.responseDelay mustEqual cacheEntry.responseTime - cacheEntry.requestTime
          }
        }

        "correctedInitialAge" >> {
          "returning correctedReceivedAge + responseDelay" >> {
            expect{
              allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime)
              allowing(responseWrapper).age willReturn Some(10.toLong)
            }
            cacheEntry.correctedInitialAge mustEqual (10 + cacheEntry.responseDelay)
          }
        }

        "residentTime" >> {
          "returning now - responseTime" >> {
            cacheEntry.residentTime mustEqual (System.currentTimeMillis - cacheEntry.responseTime)
          }
        }

        "currentAge" >> {
          "returning correctedInitialAge + residentTime" >> {
            expect{
              allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime)
              allowing(responseWrapper).age willReturn Some(10.toLong)
            }
            cacheEntry.currentAge mustEqual (cacheEntry.correctedInitialAge + cacheEntry.residentTime)
          }
        }
      }

      "expiration calculations" >> {
        "when there is a max-age directive" >> {
          "freshnessLifetime" >> {
            "returns maxAgeValue" >> {
              expect{allowing(responseWrapper).maxAge willReturn Some(1)}
              cacheEntry.freshnessLifetime mustEqual Some(1)
            }
          }
        }

        "when there is no max-age directive" >> {
          "when there is an Expires header" >> {
            "returns expiresValue - dateValue" >> {
              expect{
                allowing(responseWrapper).maxAge willReturn None
                allowing(responseWrapper).date willReturn Some(cacheEntry.responseTime)
                allowing(responseWrapper).expires willReturn Some(cacheEntry.responseTime + 10)
              }
              cacheEntry.freshnessLifetime mustEqual Some(10)
            }
          }
        }

        "isFresh" >> {
          "when freshnessLifetime >= currentAge" >> {
            "returns true" >> {
              expect{
                allowing(responseWrapper).maxAge willReturn Some(100.toLong)
                allowing(responseWrapper).age willReturn Some(1.toLong)
                allowing(responseWrapper).date willReturn None
              }
              cacheEntry.isFresh must beTrue
            }
          }

          "when freshnessLifetime < currentAge" >> {
            "returns false" >> {
              expect{
                allowing(responseWrapper).maxAge willReturn Some(1.toLong)
                allowing(responseWrapper).age willReturn Some(100.toLong)
                allowing(responseWrapper).date willReturn None
              }
              cacheEntry.isFresh must beFalse
            }
          }

          "when there is no freshnessLifetime" >> {
            "returns false" >> {
              expect{
                allowing(responseWrapper).maxAge willReturn None
                allowing(responseWrapper).expires willReturn None
              }
              cacheEntry.isFresh must beFalse
            }
          }
        }
      }

      "writeTo" >> {
        "delegates to the responseWrapper" >> {
          expect{one(responseWrapper).writeTo(response)}
          cacheEntry.writeTo(response)
        }
      }
    }
  }
}