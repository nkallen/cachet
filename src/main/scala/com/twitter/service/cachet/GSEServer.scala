package com.twitter.service.cachet

import com.google.opengse.webapp.{WebAppConfigurationBuilder, WebAppCollection, WebAppCollectionFactory, WebAppFactory}
import com.google.opengse.{ServletEngine, ServletEngineConfiguration, ServletEngineFactory, ServletEngineConfigurationImpl}
import com.google.opengse.core.ServletEngineFactoryImpl
import com.google.opengse.core.ServletEngineImpl
import net.lag.logging.Logger
import java.util.Properties
import java.io.{File, PrintWriter}
import javax.servlet.{Filter, FilterChain, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

class BasicServlet extends HttpServlet {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    val out = response.getWriter()
    out.print("Hello world!")
    out.flush()
  }
}

/**
 * Implementation of Server trait using OpenGSE.
 */
class GSEServer(val port: Int, val gracefulShutdown: Int, val numThreads: Int) extends Server {
  val engineFactory: ServletEngineFactory = new ServletEngineFactoryImpl()
  val config: ServletEngineConfiguration = ServletEngineConfigurationImpl.create(port, numThreads)
  val proxyFilter = new ProxyFilterChain()
  val engine: ServletEngine = engineFactory.createServletEngine(proxyFilter, config)
  var webapps: WebAppCollection = null

  def addServlet(servlet: Class[_ <: HttpServlet], route: String, properties: Properties) {
    //configBuilder.addServlet(servlet, route, properties)
  }

  def addServlet(servlet: Class[_ <: HttpServlet], route: String) {
    addServlet(servlet, route, new Properties())
  }

  def addServlet(servlet: HttpServlet, route: String) {
    addServlet(servlet.getClass.asInstanceOf[HttpServlet], route)
  }

  def addFilter(filter: Class[_ <: Filter], route: String) {
    //configBuilder.addFilter(filter, route)
  }

  def addFilter(filter: Filter, route: String) {
    addFilter(filter.getClass.asInstanceOf[Filter], route)
  }

  // FIXME: this should actually block until the server exits.
  def join() {}


  def start() {
    engine.run()
    // FIXME: make this timeout configurable.
    engine.awaitInitialization(10 * 1000)
  }

  def stop() {
    // FIXME: make this timeout configurable.
    engine.quit(10 * 1000) // quit with a timeout of 10 seconds.
  }
}

class ProxyFilterChain extends FilterChain {
  val log = Logger.get
  val proxy_servlet = new ProxyServlet
  proxy_servlet.init("localhost", 80, 1000, 10)

  override def doFilter(request: ServletRequest, response: ServletResponse) {
    (request, response) match {
      case (req: HttpServletRequest, res: HttpServletResponse) => {
        proxy_servlet.service(req, res)
      }
      case x => log.info("expected HttpServletRequest/Response, instead got %s so processing the next request.".format(x))
    }
  }
}
