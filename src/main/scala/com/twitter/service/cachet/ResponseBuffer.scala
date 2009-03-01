package com.twitter.service.cachet

import java.io.ByteArrayOutputStream
import java.lang.String
import javax.servlet.ServletOutputStream
import java.util.Locale
import javax.servlet.http._
import scala.collection.mutable._
import scala.util.matching.Regex

class ResponseBuffer extends HttpServletResponse {
  private val dateHeaders = new HashMap[String, Long]
  private val stringHeaders = new HashMap[String, String]
  private val intHeaders = new HashMap[String, Int]
  private val cookies = new HashSet[Cookie]
  private var statusCode = None: Option[Int]
  private var contentType = None: Option[String]
  private var locale = None: Option[Locale]
  private var contentLength = None: Option[Int]

  def addDateHeader(n: String, v: Long) {
    dateHeaders.update(n, v)
  }

  def setDateHeader(n: String, v: Long) = addDateHeader(n, v)

  def getDateHeader(n: String) =
    dateHeaders get n

  def addCookie(c: Cookie) {
    cookies += c
  }

  def addHeader(n: String, v: String) {
    stringHeaders.update(n, v)
  }

  def setHeader(n: String, v: String) = addHeader(n, v)

  def getHeader(n: String) = stringHeaders get n

  def addIntHeader(n: String, v: Int) {
    intHeaders.update(n, v)
  }

  def setIntHeader(n: String, v: Int) = addIntHeader(n, v)

  def getIntHeader(n: String) = intHeaders get n

  def sendError(sc: Int) {
    statusCode = Some(sc)
  }

  def sendError(sc: Int, msg: String) = sendError(sc)

  def setStatus(sc: Int, m: String) = sendError(sc)

  def getStatus = statusCode getOrElse 0

  def setStatus(sc: Int) = sendError(sc)

  def setContentType(ct: String) {
    contentType = Some(ct)
  }

  def getContentType = contentType getOrElse null

  def setLocale(l: Locale) {
    locale = Some(l)
  }

  def getLocale = locale getOrElse null

  def setContentLength(len: Int) {
    contentLength = Some(len)
  }

  /* */
  def flushBuffer = {}

  def reset = {}

  def resetBuffer = {}

  def isCommitted = false

  def getWriter = null

  def getOutputStream = null

  def setCharacterEncoding(charset: String) = {}

  def getCharacterEncoding = ""

  def sendRedirect(location: String) = {}

  def containsHeader(name: String) = false

  def setBufferSize(size: Int) = {}

  def getBufferSize = 0
  /* */

  /* */
  def encodeRedirectUrl(url: String) = ""

  def encodeUrl(url: String) = ""

  def encodeRedirectURL(url: String) = ""

  def encodeURL(url: String) = ""

  def isDisabled = false

  def enable {}

  def disable {}

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