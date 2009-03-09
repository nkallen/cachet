package com.twitter.service.cachet

import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletOutputStream
import java.io.{OutputStream, ByteArrayOutputStream}

class ServletOutputStreamCapturer extends ServletOutputStream {
  protected val outputStream = new ByteArrayOutputStream

  def write(b: Int) {outputStream.write(b)}

  def writeTo(response: HttpServletResponse) {
    outputStream.writeTo(response.getOutputStream)
  }
}