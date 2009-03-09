package com.twitter.service.cachet.limiter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class FakeLimitingService extends LimitingService {
  var count = 0

  def isUnderLimit(request: HttpServletRequest) = {
    count += 1
    count < 10000
  }
}