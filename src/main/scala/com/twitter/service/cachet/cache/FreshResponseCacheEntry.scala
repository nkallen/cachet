package com.twitter.service.cachet.cache

import _root_.javax.servlet.http.HttpServletResponse
import scala.util.matching.Regex

object FreshResponseCacheEntry extends Function[ResponseCapturer, FreshResponseCacheEntry] {
  def apply(responseCapturer: ResponseCapturer) = new FreshResponseCacheEntry(responseCapturer)
}

class FreshResponseCacheEntry(val responseCapturer: ResponseCapturer) extends CacheEntry {
  val requestTime = System.currentTimeMillis
  var responseTime = 0.toLong

  def noteResponseTime() {
    responseTime = System.currentTimeMillis
  }

  def dateValue = responseCapturer.date getOrElse responseTime

  def apparentAge = (responseTime - dateValue) max 0

  def ageValue = responseCapturer.age

  def correctedReceivedAge = ageValue map (_ max apparentAge) getOrElse apparentAge

  def responseDelay = responseTime - requestTime

  def correctedInitialAge = correctedReceivedAge + responseDelay

  def residentTime = System.currentTimeMillis - responseTime

  def currentAge = correctedInitialAge + residentTime

  def expiresValue = responseCapturer.expires

  def maxAgeValue = responseCapturer.maxAge

  def freshnessLifetime =
    maxAgeValue orElse (
            for (expires <- expiresValue)
            yield (expires - dateValue)
            )

  def isFresh = freshnessLifetime map (_ > currentAge) getOrElse false

  def isCachable = true

  def isTransparent = isFresh

  def writeTo(response: HttpServletResponse) {
    responseCapturer.writeTo(response)
  }
}