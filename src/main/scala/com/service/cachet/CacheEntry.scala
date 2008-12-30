package com.twitter.service.cachet

import scala.util.matching.Regex

class CacheEntry(response: ResponseWrapper) {
  val requestTime = System.currentTimeMillis
  var responseTime = 0.toLong
  
  def noteResponseTime() {
    responseTime = System.currentTimeMillis
  }
  
  def dateValue = response getDateHeader("Date")

  def apparentAge = {
    for (date <- dateValue)
      yield (responseTime - date) max 0
  }

  def ageValue = response getIntHeader("Age") map (_.toLong)

  def correctedReceivedAge = {
    for (age <- ageValue; apparent <- apparentAge)
      yield age max apparent
  }

  def responseDelay = responseTime - requestTime

  def correctedInitialAge = {
    for (corrected <- correctedReceivedAge)
      yield corrected + responseDelay
  }

  def residentTime = System.currentTimeMillis - responseTime

  def currentAge = {
    for (corrected <- correctedInitialAge)
      yield corrected + residentTime
  }

  def expiresValue = response getDateHeader("Expires")
  
  val MaxAge = """\b(?:s-maxage|max-age)=(\d+)\b""".r

  def maxAgeValue = { 
    for (cacheControl <- response getHeader("Cache-Control"); maxAge <- MaxAge findFirstMatchIn cacheControl)
      yield maxAge group(1) toLong
  }

  def freshnessLifetime = {
    maxAgeValue orElse (
      for (expires <- expiresValue; date <- dateValue)
        yield (expires - date)
    )
  }

  def isFresh = {
    for (f <- freshnessLifetime; a <- currentAge)
      yield f > a
  }
}