package com.twitter.service.cachet

import javax.servlet.http._
import scala.collection.mutable._
import scala.util.matching.Regex

class ResponseWrapper(response: HttpServletResponse) extends HttpServletResponseWrapper(response) {
  private val headers: HashMap[String, Any] = new HashMap
  private val cookies: HashSet[Cookie] = new HashSet
  private var statusCode = 0

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
}