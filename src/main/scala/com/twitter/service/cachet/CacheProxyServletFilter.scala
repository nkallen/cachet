package com.twitter.service.cachet

import _root_.javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class CacheProxyServletFilter extends Filter {
  var config: FilterConfig = null
  var get: Get = null

  def init(c: FilterConfig) {
    config = c
    get = new Get(Ehcache, Fetch(Ehcache, ResponseCapturer, CacheEntry))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    get(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}