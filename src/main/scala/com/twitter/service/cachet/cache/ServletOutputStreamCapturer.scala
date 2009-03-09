package com.twitter.service.cachet

import _root_.javax.servlet.http.HttpServletResponse
import _root_.javax.servlet.ServletOutputStream
import java.io.{OutputStream, ByteArrayOutputStream}

class ServletOutputStreamCapturer extends ServletOutputStream {
  protected val outputStream = new ByteArrayOutputStream

  def write(b: Int) {outputStream.write(b)}

  def writeTo(response: HttpServletResponse) {
    outputStream.writeTo(response.getOutputStream)
  }
}