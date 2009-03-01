package com.twitter.service.cachet.strategy

import cache.{Cache, CacheEntry}
import java.lang.String
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain

class Receive(cache: Cache, Fetch: (HttpServletRequest, HttpServletResponse, FilterChain) => CacheEntry) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    cache.get(request.getQueryString){
      Fetch(request, response, chain)
    } writeTo (response)
  }
}