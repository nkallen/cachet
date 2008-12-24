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
    headers(n).asInstanceOf[Long]
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

  def getHeader(n: String) = headers(n).asInstanceOf[String]

  override def addIntHeader(n: String, v: Int) {
    super.addIntHeader(n, v)
    headers.update(n, v)
  }

  def getIntHeader(n: String) = headers(n).asInstanceOf[Int]

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
  def apparentAge = 0 max (responseTime - dateValue).toInt
  def ageValue = getIntHeader("Age")
  def correctedReceivedAge = ageValue max apparentAge
  def responseDelay = responseTime - requestTime
  def correctedInitialAge = correctedReceivedAge + responseDelay
  def residentTime = System.currentTimeMillis - responseTime
  def currentAge = correctedInitialAge + residentTime
  
  private val CacheControl = """.*(?:(?:s-maxage=(\d+))|(?:max-age=(\d+))).*""".r
  
  def maxAgeValue = {
    val CacheControl(sMaxAge, maxAge) = headers("Cache-Control")
    (if (sMaxAge != null) sMaxAge else maxAge).toInt
  }
}