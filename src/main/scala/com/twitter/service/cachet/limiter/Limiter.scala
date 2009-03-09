package com.twitter.service.cachet.limiter

import _root_.javax.servlet.FilterChain
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class Limiter(limitingService: LimitingService) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    if (limitingService.isUnderLimit(request))
      chain.doFilter(request, response)
  }
}