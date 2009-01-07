package com.twitter.service.cachet

import scala.util.matching.Regex

class CacheEntry(val response: ResponseWrapper) {
  val requestTime = System.currentTimeMillis
  var responseTime = 0.toLong

  def noteResponseTime() {
    responseTime = System.currentTimeMillis
  }

  def dateValue = response getDateHeader("Date") getOrElse responseTime
  def apparentAge = (responseTime - dateValue) max 0
  def ageValue = response getIntHeader("Age") map (_.toLong)
  def correctedReceivedAge = ageValue map (_ max apparentAge) getOrElse apparentAge
  def responseDelay = responseTime - requestTime
  def correctedInitialAge = correctedReceivedAge + responseDelay
  def residentTime = System.currentTimeMillis - responseTime
  def currentAge = correctedInitialAge + residentTime
  def expiresValue = response getDateHeader("Expires")

  private val MaxAge = """\b(?:s-maxage|max-age)=(\d+)\b""".r

  def maxAgeValue = {
    for (cacheControl <- response getHeader("Cache-Control"); maxAge <- MaxAge findFirstMatchIn cacheControl)
      yield maxAge group(1) toLong
  }

  def freshnessLifetime = {
    maxAgeValue orElse (
      for (expires <- expiresValue)
        yield (expires - dateValue)
    )
  }

  def isFresh = freshnessLifetime map (_ > currentAge) getOrElse false
  def isCachable = true
  def isTransparent = true
}