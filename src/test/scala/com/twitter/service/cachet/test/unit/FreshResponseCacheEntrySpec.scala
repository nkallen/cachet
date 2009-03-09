package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet.cache.FreshResponseCacheEntry
import com.twitter.service.cachet._
import javax.servlet.http._
import org.jmock.core.Stub
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object FreshResponseCacheEntrySpec extends Specification with JMocker with ClassMocker {
  "FreshResponseCacheEntry" should {
    var responseCapturer = null: ResponseCapturer
    var freshResponseCacheEntry = null: FreshResponseCacheEntry
    var response = null: HttpServletResponse
    val millis = System.currentTimeMillis

    "implement RFC 2616" >> {
      doBefore{
        response = mock[HttpServletResponse]
        responseCapturer = mock[ResponseCapturer]
        expect{
          allowing(responseCapturer).requestTime willReturn millis
          allowing(responseCapturer).responseTime willReturn millis
        }
        freshResponseCacheEntry = new FreshResponseCacheEntry(responseCapturer, () => millis)
      }

      "age calculations" >> {
        "dateValue" >> {
          "when there is a Date header" >> {
            "returns the value of the header" >> {
              val millis = System.currentTimeMillis
              expect{allowing(responseCapturer).date willReturn Some(millis)}
              freshResponseCacheEntry.dateValue mustEqual millis
            }
          }

          "when there is no Date header" >> {
            "returns the response time" >> {
              expect{allowing(responseCapturer).date willReturn None}
              freshResponseCacheEntry.dateValue mustEqual freshResponseCacheEntry.responseTime
            }
          }
        }

        "apparentAge" >> {
          "when dateValue <= responseTime" >> {
            "returns responseTime - dateValue" >> {
              expect{allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime - 10)}
              freshResponseCacheEntry.apparentAge mustEqual 10
            }
          }

          "when dateValue > responseTime" >> {
            "returns 0" >> {
              expect{allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime + 10)}
              freshResponseCacheEntry.apparentAge mustEqual 0
            }
          }
        }

        "correctedReceivedAge" >> {
          "when apparentAge > ageValue" >> {
            "returns apparentAge" >> {
              expect{
                allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime + 1)
                allowing(responseCapturer).age willReturn Some(0.toLong)
              }
              freshResponseCacheEntry.correctedReceivedAge mustEqual freshResponseCacheEntry.apparentAge
            }
          }

          "when apparentAge < ageValue" >> {
            "returns ageValue" >> {
              expect{
                allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime)
                allowing(responseCapturer).age willReturn Some(1.toLong)
              }
              freshResponseCacheEntry.correctedReceivedAge mustEqual 1
            }
          }

          "when no ageValue" >> {
            "returns apparentAge" >> {
              expect{
                allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime)
                allowing(responseCapturer).age willReturn None
              }
              freshResponseCacheEntry.correctedReceivedAge mustEqual freshResponseCacheEntry.apparentAge
            }
          }
        }

        "responseDelay" >> {
          "returning responseTime - requestTime" >> {
            freshResponseCacheEntry.responseDelay mustEqual freshResponseCacheEntry.responseTime - freshResponseCacheEntry.requestTime
          }
        }

        "correctedInitialAge" >> {
          "returning correctedReceivedAge + responseDelay" >> {
            expect{
              allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime)
              allowing(responseCapturer).age willReturn Some(10.toLong)
            }
            freshResponseCacheEntry.correctedInitialAge mustEqual (10 + freshResponseCacheEntry.responseDelay)
          }
        }

        "residentTime" >> {
          "returning now - responseTime" >> {
            freshResponseCacheEntry.residentTime mustEqual (millis - freshResponseCacheEntry.responseTime)
          }
        }

        "currentAge" >> {
          "returning correctedInitialAge + residentTime" >> {
            expect{
              allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime)
              allowing(responseCapturer).age willReturn Some(10.toLong)
            }
            freshResponseCacheEntry.currentAge mustEqual (freshResponseCacheEntry.correctedInitialAge + freshResponseCacheEntry.residentTime)
          }
        }
      }

      "expiration calculations" >> {
        "when there is a max-age directive" >> {
          "freshnessLifetime" >> {
            "returns maxAgeValue" >> {
              expect{allowing(responseCapturer).maxAge willReturn Some(1)}
              freshResponseCacheEntry.freshnessLifetime mustEqual Some(1)
            }
          }
        }

        "when there is no max-age directive" >> {
          "when there is an Expires header" >> {
            "returns expiresValue - dateValue" >> {
              expect{
                allowing(responseCapturer).maxAge willReturn None
                allowing(responseCapturer).date willReturn Some(freshResponseCacheEntry.responseTime)
                allowing(responseCapturer).expires willReturn Some(freshResponseCacheEntry.responseTime + 10)
              }
              freshResponseCacheEntry.freshnessLifetime mustEqual Some(10)
            }
          }
        }

        "isFresh" >> {
          "when freshnessLifetime >= currentAge" >> {
            "returns true" >> {
              expect{
                allowing(responseCapturer).maxAge willReturn Some(100.toLong)
                allowing(responseCapturer).age willReturn Some(1.toLong)
                allowing(responseCapturer).date willReturn None
              }
              freshResponseCacheEntry.isFresh must beTrue
            }
          }

          "when freshnessLifetime < currentAge" >> {
            "returns false" >> {
              expect{
                allowing(responseCapturer).maxAge willReturn Some(1.toLong)
                allowing(responseCapturer).age willReturn Some(100.toLong)
                allowing(responseCapturer).date willReturn None
              }
              freshResponseCacheEntry.isFresh must beFalse
            }
          }

          "when there is no freshnessLifetime" >> {
            "returns false" >> {
              expect{
                allowing(responseCapturer).maxAge willReturn None
                allowing(responseCapturer).expires willReturn None
              }
              freshResponseCacheEntry.isFresh must beFalse
            }
          }
        }
      }

      "writeTo" >> {
        "delegates to the responseCapturer" >> {
          expect{one(responseCapturer).writeTo(response)}
          freshResponseCacheEntry.writeTo(response)
        }
      }
    }
  }
}