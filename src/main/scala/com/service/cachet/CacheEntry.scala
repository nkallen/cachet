package com.twitter.service.cachet

import javax.servlet.http._
import scala.collection.mutable._
import scala.util.matching.Regex

class CacheEntry(response: HttpServletResponse) extends HttpServletResponseWrapper(response) {
  private val headers: HashMap[String, Any] = new HashMap
  private val cookies: HashSet[Cookie] = new HashSet
  private var statusCode = 0
  
  val requestTime = System.currentTimeMillis
  var responseTime = 0.toLong

  override def addDateHeader(n: String, v: Long) {
    super.addDateHeader(n, v)
    headers.update(n, v)
  }

  def getDateHeader(n: String) = {
    headers get n map (_.asInstanceOf[Long])
  }

  override def addCookie(c: Cookie) {
    super.addCookie(c)
    cookies += c
  }

  def getCookies = cookies

  override def addHeader(n: String, v: String) {
    super.addHeader(n, v)
    headers.update(n, v)
  }

  def getHeader(n: String) = headers get n map (_.asInstanceOf[String])

  override def addIntHeader(n: String, v: Int) {
    super.addIntHeader(n, v)
    headers.update(n, v)
  }

  def getIntHeader(n: String) = headers get n map (_.asInstanceOf[Int])

  override def sendError(sc: Int) {
    super.sendError(sc)
    statusCode = sc
  }

  def getStatus = statusCode

  override def setDateHeader(n: String, v: Long) = addDateHeader(n, v)
  override def setHeader(n: String, v: String) = addHeader(n, v)
  override def setIntHeader(n: String, v: Int) = addIntHeader(n, v)
  override def setStatus(sc: Int) = sendError(sc)

  def noteResponseTime() {
    responseTime = System.currentTimeMillis
  }
  
  def dateValue = getDateHeader("Date")

  def apparentAge = {
    for (date <- dateValue)
      yield 0 max (responseTime - date).toInt
  }

  def ageValue = getIntHeader("Age")

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

  def expiresValue = getDateHeader("Expires")
  
  val MaxAge = """\b(?:s-maxage|max-age)=(\d+)\b""".r

  def maxAgeValue = { 
    for (cacheControl <- getHeader("Cache-Control"); maxAge <- MaxAge findFirstMatchIn cacheControl)
      yield maxAge group(1) toInt
  }

  def freshnessLifetime = {
    maxAgeValue orElse (
      for (expires <- expiresValue; date <- dateValue)
        yield (expires - date) toInt
    )
  }

  def isFresh = {
    for (f <- freshnessLifetime; a <- currentAge)
      yield f > a
  }
}