package com.twitter.service.cachet

import java.io.ByteArrayOutputStream
import java.lang.String
import javax.servlet.ServletOutputStream
import java.util.Locale
import javax.servlet.http._
import scala.collection.mutable._
import scala.util.matching.Regex

class ResponseWrapper(response: HttpServletResponse) extends HttpServletResponseWrapper(response) {
  private val dateHeaders = new HashMap[String, Long]
  private val stringHeaders = new HashMap[String, String]
  private val intHeaders = new HashMap[String, Int]
  private val cookies = new HashSet[Cookie]
  private var statusCode = None: Option[Int]
  private var contentType = None: Option[String]
  private var locale = None: Option[Locale]
  private var contentLength = None: Option[Int]

  override def addDateHeader(n: String, v: Long) {
    dateHeaders.update(n, v)
  }

  override def setDateHeader(n: String, v: Long) = addDateHeader(n, v)

  def getDateHeader(n: String) =
    dateHeaders get n

  override def addCookie(c: Cookie) {
    cookies += c
  }

  override def addHeader(n: String, v: String) {
    stringHeaders.update(n, v)
  }

  override def setHeader(n: String, v: String) = addHeader(n, v)

  def getHeader(n: String) = stringHeaders get n

  override def addIntHeader(n: String, v: Int) {
    intHeaders.update(n, v)
  }

  override def setIntHeader(n: String, v: Int) = addIntHeader(n, v)

  def getIntHeader(n: String) = intHeaders get n

  override def sendError(sc: Int) {
    statusCode = Some(sc)
  }

  override def sendError(sc: Int, msg: String) = sendError(sc)

  override def setStatus(sc: Int, m: String) = sendError(sc)

  def getStatus = statusCode getOrElse 0

  override def setStatus(sc: Int) = sendError(sc)

  override def setContentType(ct: String) {
    contentType = Some(ct)
  }

  override def getContentType = contentType getOrElse null

  override def setLocale(l: Locale) {
    locale = Some(l)
  }

  override def getLocale = locale getOrElse null

  override def setContentLength(len: Int) {
    contentLength = Some(len)
  }

  override def getWriter = null

  override def getOutputStream = null

  override def setCharacterEncoding(charset: String) = {}

  override def getCharacterEncoding = ""

  /* FIXME - implement these: */
  override def flushBuffer = {}

  override def reset = {}

  override def resetBuffer = {}

  override def isCommitted = false

  override def sendRedirect(location: String) = {}

  override def containsHeader(name: String) = false

  override def setBufferSize(size: Int) = {}

  override def getBufferSize = 0
  /* */

  def writeTo(response: HttpServletResponse) {
    for ((key, value) <- dateHeaders)
      response.addDateHeader(key, value)
    for ((key, value) <- stringHeaders)
      response.addHeader(key, value)
    for ((key, value) <- intHeaders)
      response.addIntHeader(key, value)
    for (cookie <- cookies)
      response.addCookie(cookie)
    for (sc <- statusCode)
      response.setStatus(sc)
    for (ct <- contentType)
      response.setContentType(ct)
    for (l <- contentLength)
      response.setContentLength(l)
    for (l <- locale)
      response.setLocale(l)
  }
}