package com.twitter.service.cachet.limiter

import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class Limiter(limitingService: LimitingService) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    if (limitingService.isUnderLimit(request)) {
      chain.doFilter(request, response)
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }
  }
}