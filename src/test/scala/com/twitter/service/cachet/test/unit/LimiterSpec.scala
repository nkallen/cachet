package com.twitter.service.cachet.test.unit

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import limiter.{Limiter, LimitingService}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object LimiterSpec extends Specification with JMocker {
  "Limiter" should {
    var limitingService = null: LimitingService
    var limiter = null: Limiter
    var request = null: HttpServletRequest
    var response = null: HttpServletResponse
    var chain = null: FilterChain

    doBefore{
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]
      chain = mock[FilterChain]
      limitingService = mock[LimitingService]
      limiter = new Limiter(limitingService)
    }

    "when the request is under the limit" >> {
      "applies the chain" >> {
        expect{
          allowing(limitingService).isUnderLimit(request) willReturn true
          one(chain).doFilter(request, response)
        }
        limiter(request, response, chain)
      }
    }

    "when the request is over the limit" >> {
      "applies the chain" >> {
        expect{
          allowing(limitingService).isUnderLimit(request) willReturn false
          one(response).setStatus(HttpServletResponse.SC_BAD_REQUEST)
          never(chain).doFilter(request, response)
        }
        limiter(request, response, chain)
      }
    }
  }
}