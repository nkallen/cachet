package com.twitter.service.cachet.cache

import javax.servlet.http.HttpServletResponse
import scala.util.matching.Regex

object FreshResponseCacheEntry extends Function[ResponseCapturer, FreshResponseCacheEntry] {
  def apply(responseCapturer: ResponseCapturer) = new FreshResponseCacheEntry(responseCapturer, () => System.currentTimeMillis)
}

class FreshResponseCacheEntry(val responseCapturer: ResponseCapturer, now: () => Long) extends CacheEntry {
  val requestTime = responseCapturer.requestTime
  var responseTime = responseCapturer.responseTime

  def dateValue = responseCapturer.date getOrElse responseTime

  def apparentAge = (responseTime - dateValue) max 0

  def ageValue = responseCapturer.age

  def correctedReceivedAge = ageValue map (_ max apparentAge) getOrElse apparentAge

  def responseDelay = responseTime - requestTime

  def correctedInitialAge = correctedReceivedAge + responseDelay

  def residentTime = now() - responseTime

  def currentAge = correctedInitialAge + residentTime

  def expiresValue = responseCapturer.expires

  def maxAgeValue = responseCapturer.maxAge

  def freshnessLifetime =
    maxAgeValue orElse (
            for (expires <- expiresValue)
            yield (expires - dateValue)
            )

  def isFresh = freshnessLifetime map (_ > currentAge) getOrElse false

  def isCachable = isFresh && Array(
    HttpServletResponse.SC_OK,
    HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION,
    HttpServletResponse.SC_MULTIPLE_CHOICES,
    HttpServletResponse.SC_MOVED_PERMANENTLY,
    HttpServletResponse.SC_MOVED_TEMPORARILY,
    HttpServletResponse.SC_TEMPORARY_REDIRECT,
    HttpServletResponse.SC_GONE).contains(responseCapturer.getStatusCode)

  def isTransparent = isFresh

  def writeTo(response: HttpServletResponse) {
    responseCapturer.writeTo(response)
  }
}