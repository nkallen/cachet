package com.twitter.service.cachet.servlet

import javax.servlet._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import strategy.Receive

class CacheProxyServletFilter extends Filter {
  var config: FilterConfig = null
  var get: Receive = null

  def init(c: FilterConfig) {
    config = c
    get = new Receive(Ehcache, new Fetch(Ehcache, ResponseCapturer, CacheEntry)(_, _, _))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    get(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}