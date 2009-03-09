package com.twitter.service.cachet.limiter

import cache._
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

class LimitingProxyServletFilter extends Filter {
  var config = null: FilterConfig
  var limiter = null: Limiter

  def init(c: FilterConfig) {
    config = c
    limiter = new Limiter(new FakeLimitingService)
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    limiter(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}