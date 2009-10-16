package com.twitter.service.cachet.proxy

import net.lag.logging.Logger
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Hashtable
import java.net.URLDecoder
import javax.servlet.ServletInputStream
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}

/**
 * HttpServletRequestWrapper that encapsulates the underlying InputStream so that it can be read multiple times.
 *
 * The main use is so that ServletFilters can read the InputStream without blocking Servlets from also reading it.
 */
class BufferedRequestWrapper(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
  private val log = Logger.get
  val is = req.getInputStream
  val baos = new ByteArrayOutputStream()
  val buf = new Array[Byte](4 * 1024)
  var letti = is.read(buf)

  while (letti > 0) {
    baos.write(buf, 0, letti)
    letti = is.read(buf)
  }

  val buffer = baos.toByteArray()
  log.debug("BufferedRequestWrapper read %s bytes", buffer.size)

  override def getInputStream(): ServletInputStream = new BufferedServletInputStream(new ByteArrayInputStream(buffer))

  override def getParameter(param: String): String = {
    val queryMap = parseQueryString(this.getQueryString)
    if (queryMap.contains(param)) {
      queryMap.getOrElse(param, null)
    } else if (req.getMethod == "POST" && req.getContentType != null && req.getContentType.startsWith("application/x-www-form-urlencoded")) {
      parseQueryString(new String(buffer)).getOrElse(param, null)
    } else {
      null
    }
  }

  /**
   * FIXME: multiple key-value pairs are not supported in this version.
   */
  def parseQueryString(queryString: String): Map[String, String] = {
    if (queryString != null) {
      val decoded = URLDecoder.decode(queryString)
      val elements: Array[(String, String)] = if (decoded.contains("&")) {
        val y = decoded.split("&").map(_.split("="))
        y.filter(_.size >= 2).map(array => (array(0), array(1)))
      } else if (decoded.contains("=")) {
        val z = decoded.split("=")
        if (z.size >= 2) {
          Array(z(0) -> z(1))
        } else {
          Array()
        }
      } else {
        Array()
      }
      Map.empty ++ elements
    } else {
      Map.empty
    }
  }
}

/**
 * Simply delegates all methods to the underlying ByteArrayInputStream.
 */
class BufferedServletInputStream(bais: ByteArrayInputStream) extends ServletInputStream {
  override def available() = bais.available

  override def read() = bais.read()

  override def read(buf: Array[Byte], off: Int, len: Int): Int = bais.read(buf, off, len)
}
