package com.twitter.service.cachet

import java.io.{OutputStreamWriter, PrintWriter, ByteArrayOutputStream}
import java.lang.String
import javax.servlet.ServletOutputStream
import java.util.Locale
import javax.servlet.http._
import scala.collection.mutable._
import scala.util.matching.Regex

object ResponseCapturer extends Function2[HttpServletResponse, (HttpServletResponse => Unit), ResponseCapturer] {
  def apply(response: HttpServletResponse, responder: HttpServletResponse => Unit) = {
    val responseCapturer = new ResponseCapturer(response, new ServletOutputStreamCapturer)
    responder(responseCapturer)
    responseCapturer.noteResponseTime()
    responseCapturer
  }
}

class ResponseCapturer(response: HttpServletResponse, servletOutputStreamCapturer: ServletOutputStreamCapturer) extends HttpServletResponseWrapper(response) {
  private val dateHeaders = new HashMap[String, Long]
  private val stringHeaders = new HashMap[String, String]
  private val intHeaders = new HashMap[String, Int]
  private val cookies = new HashSet[Cookie]
  private var characterEncoding = None: Option[String]
  private var writer = None: Option[PrintWriter]
  private var statusCode = None: Option[Int]
  private var contentType = None: Option[String]
  private var locale = None: Option[Locale]
  private var contentLength = None: Option[Int]

  val requestTime = System.currentTimeMillis
  var responseTime = 0.toLong

  def noteResponseTime() {
    responseTime = System.currentTimeMillis
  }

  def getStatusCode = statusCode getOrElse 0

  override def addDateHeader(n: String, v: Long) {
    dateHeaders.update(n, v)
  }

  override def setDateHeader(n: String, v: Long) = addDateHeader(n, v)

  def date = dateHeaders get "Date"

  def expires = dateHeaders get "Expires"

  private val MaxAge = """\b(?:s-maxage|max-age)=(\d+)\b""".r

  def maxAge =
    for (cacheControl <- stringHeaders.get("Cache-Control");
         maxAge <- MaxAge findFirstMatchIn cacheControl)
    yield maxAge group (1) toLong

  def age = intHeaders get "Age" map (_.toLong)

  override def addCookie(c: Cookie) {
    cookies += c
  }

  override def addHeader(n: String, v: String) {
    stringHeaders.update(n, v)
  }

  override def setHeader(n: String, v: String) = addHeader(n, v)

  override def addIntHeader(n: String, v: Int) {
    intHeaders.update(n, v)
  }

  override def setIntHeader(n: String, v: Int) = addIntHeader(n, v)

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

  override def getWriter = {
    writer getOrElse {
      val printWriter = new PrintWriter(new OutputStreamWriter(servletOutputStreamCapturer, getCharacterEncoding), true)
      writer = Some(printWriter)
      printWriter
    }
  }

  override def getOutputStream = servletOutputStreamCapturer

  override def setCharacterEncoding(charset: String) = {
    characterEncoding = Some(charset)
  }

  /* FIXME - implement these: */
  override def getCharacterEncoding = characterEncoding getOrElse "ISO-8859-1" // this needs to consider contentType and locale
  override def sendRedirect(location: String) = {}

  override def containsHeader(name: String) = false
  /* */

  override def flushBuffer = {}

  override def reset = {}

  override def resetBuffer = {}

  override def isCommitted = false

  override def getBufferSize = 0

  override def setBufferSize(size: Int) = {}

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
    for (ce <- characterEncoding)
      response.setCharacterEncoding(ce)

    flush()
    servletOutputStreamCapturer.writeTo(response)
  }

  private def flush() {
    writer map (_.flush())
  }
}