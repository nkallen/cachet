package com.twitter.service.cachet

import _root_.javax.servlet.http.HttpServletResponse
import _root_.javax.servlet.ServletOutputStream
import java.io.{OutputStream, ByteArrayOutputStream}

class ByteArrayServletOutputStreamCapturer extends ServletOutputStreamCapturer {
  protected val outputStream = new ByteArrayOutputStream

  def write(b: Int) {outputStream.write(b)}

  def writeTo(response: HttpServletResponse) {
    if (outputStream.size > 0)
      outputStream.writeTo(response.getOutputStream)
  }
}