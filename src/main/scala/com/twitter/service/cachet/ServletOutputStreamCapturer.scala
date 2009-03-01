package com.twitter.service.cachet

import _root_.javax.servlet.http.HttpServletResponse
import _root_.javax.servlet.ServletOutputStream

trait ServletOutputStreamCapturer extends ServletOutputStream {
  def write(b: Int)
  def writeTo(response: HttpServletResponse)
}