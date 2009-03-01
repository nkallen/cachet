package com.twitter.service.cachet

import _root_.javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

class CacheProxyServletFilter extends Filter {
  var config: FilterConfig = null
  var proxy: Get = null

  def init(c: FilterConfig) {
    config = c
    //    proxy = new Get(Ehcache, ResponseCapturer, CacheEntry)
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    //    proxy(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}