package com.twitter.service.cachet.proxy

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.servlet.ServletInputStream
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}

/**
 * HttpServletRequestWrapper that encapsulates the underlying InputStream so that it can be read multiple times.
 *
 * The main use is so that ServletFilters can read the InputStream without blocking Servlets from also reading it.
 */
class BufferedRequestWrapper(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
  val is = req.getInputStream
  val baos = new ByteArrayOutputStream()
  val buf = new Array[Byte](1024)
  var letti = is.read(buf)

  while (letti > 0) {
    baos.write(buf, 0, letti)
    letti = is.read(buf)
  }

  val buffer = baos.toByteArray()

  override def getInputStream(): ServletInputStream = new BufferedServletInputStream(new ByteArrayInputStream(buffer))
}

/**
 * Simply delegates all methods to the underlying ByteArrayInputStream.
 */
class BufferedServletInputStream(bais: ByteArrayInputStream) extends ServletInputStream {
  override def available() = bais.available

  override def read() = bais.read()

  override def read(buf: Array[Byte], off: Int, len: Int): Int = bais.read(buf, off, len)
}
