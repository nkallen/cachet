package com.twitter.service.cachet.limiter

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class FakeLimitingService extends LimitingService {
  def isUnderLimit(request: HttpServletRequest) = false
}