package com.twitter.service.cachet

import _root_.javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import net.sf.ehcache.CacheManager

class CacheProxyServletFilter extends Filter {
  var config: FilterConfig = null
  var proxy: CacheProxy = null

  def init(c: FilterConfig) {
    config = c
    val cache = CacheManager.getInstance.getEhcache("Name")
    proxy = new CacheProxy(cache, CacheEntry(_))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    proxy(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)    
  }

  def destroy {}
}