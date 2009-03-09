package com.twitter.service.cachet.test.mock

import scala.collection.mutable.HashMap
import java.io.PrintWriter
import java.io.StringReader
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.StringBufferInputStream
import java.io.File
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.Vector
import javax.servlet._
import javax.servlet.http._

/**
 * A Mock ServletRequest. Change it's state to to create the request you are
 * interested in. At the very least, you will need to change method and path.
 *
 * @author Steve Jenson (stevej@pobox.com)
 */
class FakeHttpServletRequest extends HttpServletRequest {
  var session: HttpSession = new MockHttpSession
  var queryString: String = ""
  var contextPath = ""
  var path = ""
  var method = "GET"
  val headers: scala.collection.jcl.HashMap[String, String] =
  new scala.collection.jcl.HashMap[String, String](new java.util.HashMap)
  val attr: scala.collection.jcl.HashMap[String, Any] =
  new scala.collection.jcl.HashMap[String, Any](new java.util.HashMap)
  var cookies: List[Cookie] = Nil
  var authType = null
  var localPort = 0
  var localAddr = null
  var localName = null
  var remotePort = 0
  var remoteHost = null
  var remoteAddr = null: String
  var isInitial = true
  var locale = Locale.getDefault
  var reader: BufferedReader = new BufferedReader(new StringReader(method + " " + path + "/\r\n\r\n"))
  var serverPort = 0
  var serverName = null
  var scheme = "http"
  var protocol = "http 1.0"
  var parameterMap: scala.collection.jcl.HashMap[String, String] =
  new scala.collection.jcl.HashMap[String, String](new java.util.HashMap)
  val sbis = new StringBufferInputStream("")
  var inputStream: ServletInputStream = new MockServletInputStream(sbis)
  var contentType = null
  var contentLength = 0
  var charEncoding = "ISO-8859-1" // HTTP's default encoding
  var isTimeout = false
  var isResumed = false
  var isSuspended = false

  def complete {}

  def resume {}

  def suspend {}

  def suspend(l: Long) {}

  def isRequestedSessionIdFromURL = false

  def isRequestedSessionIdFromUrl = false

  def isRequestedSessionIdFromCookie = false

  def isRequestedSessionIdValid = false

  def getSession(p: Boolean) = {
    session
  }

  def getSession = getSession(false)

  def getServletPath = ""

  def getRequestURL = new StringBuffer(path)

  def getRequestURI = path

  def getRequestedSessionId = null

  def getUserPrincipal = null

  def isUserInRole(user: String): Boolean = false

  def getRemoteUser = ""

  def getQueryString = queryString

  def getContextPath = contextPath

  def getPathTranslated = path

  def getPathInfo = path

  def getMethod = method

  def getIntHeader(h: String): Int = {
    headers(h).toInt
  }

  def getHeaderNames = {
    new Vector[AnyRef](headers.underlying.keySet).elements
  }

  def getHeaders = headers

  def getHeaders(s: String) = {
    val v = new Vector[AnyRef]()
    v.add(headers(s))
    v.elements
  }

  def getHeader(h: String) = headers.get(h) match {
    case Some(v) => v
    case None => null
  }

  def getDateHeader(h: String): Long = {
    headers(h).toLong
  }

  def setDateHeader(s: String, l: Long) {
    headers += (s -> l.toString)
  }

  def setHeader(k: String, v: String) {
    headers += (k -> v)
  }

  def getCookies = cookies.toArray

  def getAuthType = authType

  def getLocalPort = localPort

  def getLocalAddr = localAddr

  def getLocalName = localName

  def getRemotePort = remotePort

  def getRealPath(s: String) = s

  def getRequestDispatcher(s: String): RequestDispatcher = null

  def isSecure = false

  type ZZ = Q forSome {type Q}

  def getLocales = new Vector[ZZ](Arrays.asList(Locale.getAvailableLocales: _*)).elements

  def getLocale = locale

  def removeAttribute(key: String) = attr -= key

  def setAttribute(key: String, value: Any) = attr += (key -> value)

  def getRemoteHost = remoteHost

  def getRemoteAddr = remoteAddr

  def getReader = reader

  def getServerPort = serverPort

  def getServerName = serverName

  def getScheme = scheme

  def getProtocol = protocol

  def getParameterMap = parameterMap.underlying

  def getParameterValues(key: String) =
    parameterMap.underlying.values.toArray.asInstanceOf[Array[String]]

  def getParameterNames = new Vector[ZZ](parameterMap.underlying.keySet.asInstanceOf[java.util.Set[ZZ]]).elements

  def getParameter(key: String) = parameterMap(key)

  def getInputStream = inputStream

  def getContentType = contentType

  def getContentLength = contentLength

  def getCharacterEncoding = charEncoding

  def setCharacterEncoding(enc: String) = charEncoding = enc

  def getAttributeNames = new Vector[ZZ](attr.underlying.keySet.asInstanceOf[java.util.Set[ZZ]]).elements

  def getAttribute(key: String) = attr(key).asInstanceOf[Object]

  def getServletContext = null

  def getServletResponse = null
}