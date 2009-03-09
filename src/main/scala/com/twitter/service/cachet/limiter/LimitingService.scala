package com.twitter.service.cachet.limiter

import _root_.javax.servlet.http.HttpServletRequest

trait LimitingService {
  def isUnderLimit(request: HttpServletRequest): Boolean
}