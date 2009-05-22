package com.twitter.service.cachet

import proxy.client.{ApacheHttpClient, ForwardRequest, JettyHttpClient}
import com.twitter.commons.W3CStats
import net.lag.logging.Logger
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import java.net.ConnectException
import java.util.Date

class ProxyServlet extends HttpServlet {
  var config = null: ServletConfig
  var httpForwardRequest = null: ForwardRequest
  var httpsForwardRequest = null: ForwardRequest
  var host: String = ""
  var port: Int = 0
  var sslPort: Int = 10433
  var timeout: Long = 0L
  var numThreads: Int = 0
  private val log = Logger.get // FIXME: use a separate logfile

  def init(backend_host: String, backend_port: Int, backend_ssl_port: Int, backend_timeout: Long , num_threads: Int, use_apache: Boolean) {
    this.host = backend_host
    this.port = backend_port
    this.sslPort = backend_ssl_port
    this.timeout = backend_timeout
    this.numThreads = num_threads

    val client = if (use_apache) {
      new ApacheHttpClient(timeout, numThreads, port, sslPort)
    } else {
      new JettyHttpClient(timeout, numThreads)
    }

    log.info("Instantiating HttpClients (%s) use_apache = %s, host = %s, port = %d, ssl_port=%s timeout = %d, threads = %d ", client,
             use_apache, host, port, sslPort, timeout, numThreads)

    httpForwardRequest = new ForwardRequest(client, host, port)
    httpsForwardRequest = new ForwardRequest(client, host, sslPort)
  }

  override def init(c: ServletConfig) {
    config = c
    val _host = config.getInitParameter("backend-host") match {
      case null => "localhost"
      case x: String => x
    }

    val _port = config.getInitParameter("backend-port") match {
      case null => 20080
      case x: String => x.toInt
    }

    val _sslPort = config.getInitParameter("backend-ssl-port") match {
      case null => 20443
      case x: String => x.toInt
    }

    val _timeout = config.getInitParameter("backend-timeout") match {
      case null => 1000L
      case x: String => x.toLong
    }

    val _numThreads = config.getInitParameter("backend-numthreads") match {
      case null => 10
      case x: String => x.toInt
    }

    val _useApache: Boolean = config.getInitParameter("use-apache-client") match {
      case null => true
      case x: String => x.toBoolean
    }

    init(_host, _port, _sslPort, _timeout, _numThreads, _useApache)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val datetime = Stats.w3c.datetime_format(new Date())
    Stats.w3c.log("request-date", datetime._1)
    Stats.w3c.log("request-time", datetime._2)
    Stats.w3c.log("remote-ip", request.getRemoteAddr())
    try {
      Stats.w3c.time("rs-response-time") {
        try {
          if (request.getScheme.equals("http")) {
            httpForwardRequest(request, response)
          } else {
            httpsForwardRequest(request, response)
          }
        } catch {
          case c: ConnectException => {
            log.error("unable to connect to backend")
          }
          case e => {
            log.error(e, "unable to connect to backend with uncaught exception: %s with optional cause: %s".format(e, e.getCause))
          }
        }
      }
    } finally {
      log.info(Stats.w3c.log_entry)
      Stats.w3c.clear
    }
  }
}

/**
 * At the end of each request, print the w3c log line and clear the w3c log store.
 */
class LoggingFilter extends Filter {
  private val log = Logger.get // FIXME: use a separate logfile
  log.info("Instantiating logging filter %s".format(this))
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    chain.doFilter(request, response)
  }

  def init(filterConfig: FilterConfig) { /* nothing */ }

  def destroy() { /* nothing */ }
}

class BasicFilter extends Filter {
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    response.getWriter().append("yo").append(" - ")
    chain.doFilter(request, response)
  }

  def init(filterConfig: FilterConfig) { /* nothing */ }

  def destroy() { /* nothing */ }
}

