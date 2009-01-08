package com.twitter.service.cachet

object Main {
  def main(args: Array[String]) {
    val server = new Server
    server.start()
  }
}