package com.twitter.service.cachet.limiter

import javax.servlet.http.HttpServletRequest

trait LimitingService {
  def isUnderLimit(request: HttpServletRequest): Boolean
}