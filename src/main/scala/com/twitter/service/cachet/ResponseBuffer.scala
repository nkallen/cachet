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
  private var statusCode = 0
  private var contentType = ""
  private var locale: Locale = null
  private var contentLength = 0

  def addDateHeader(n: String, v: Long) {
    dateHeaders.update(n, v)
  }

  def getDateHeader(n: String) =
    dateHeaders get n

  def addCookie(c: Cookie) {
    cookies += c
  }

  def addHeader(n: String, v: String) {
    stringHeaders.update(n, v)
  }

  def getHeader(n: String) = stringHeaders get n

  def addIntHeader(n: String, v: Int) {
    intHeaders.update(n, v)
  }

  def getIntHeader(n: String) = intHeaders get n

  def sendError(sc: Int) {
    statusCode = sc
  }

  def getStatus = statusCode

  def setDateHeader(n: String, v: Long) = addDateHeader(n, v)

  def setHeader(n: String, v: String) = addHeader(n, v)

  def setIntHeader(n: String, v: Int) = addIntHeader(n, v)

  def setStatus(sc: Int) = sendError(sc)

  def encodeRedirectUrl(url: String) = ""

  def encodeUrl(url: String) = ""

  def flushBuffer = {}

  def getOutputStream = null

  def setBufferSize(size: Int) = {}

  def getBufferSize = 0

  def reset = {}

  def resetBuffer = {}

  def getCharacterEncoding = ""

  def isCommitted = false

  def getWriter = null

  def setCharacterEncoding(charset: String) = {}

  def containsHeader(name: String) = false

  def sendError(sc: Int, msg: String) = {}

  def sendRedirect(location: String) = {}

  def encodeRedirectURL(url: String) = ""

  def encodeURL(url: String) = ""

  def setStatus(sc: Int, m: String) = sendError(sc)

  def setContentType(ct: String) {
    contentType = ct
  }

  def isDisabled = false

  def enable {}

  def disable {}

  def getContentType = contentType

  def setLocale(l: Locale) {
    locale = l
  }

  def getLocale = locale

  def setContentLength(len: Int) {
    contentLength = len
  }

  def getContentLength = contentLength

  def writeTo(response: HttpServletResponse) {
    for ((key, value) <- dateHeaders)
      response.addDateHeader(key, value)
    for ((key, value) <- stringHeaders)
      response.addHeader(key, value)
    for ((key, value) <- intHeaders)
      response.addIntHeader(key, value)
    for (cookie <- cookies)
      response.addCookie(cookie)
  }
}