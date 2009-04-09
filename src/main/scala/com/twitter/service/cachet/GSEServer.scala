package com.twitter.service.cachet

import com.google.opengse.webapp.{WebAppConfigurationBuilder, WebAppCollection, WebAppCollectionFactory, WebAppFactory}
import com.google.opengse.{ServletEngine,ServletEngineConfiguration, ServletEngineConfigurationImpl}
import com.google.opengse.core.ServletEngineImpl
import java.util.Properties
import java.io.{File, PrintWriter}
import javax.servlet.{Filter, FilterChain}
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
class GSEServer(val port: Int) extends Server {
  val max_threads = 5
  val configBuilder = new WebAppConfigurationBuilder()

  val props = new Properties()
  props.setProperty("context", "foo") // FIXME: change this to handle ALL incoming requests.
  props.setProperty("contextdir", System.getProperty("java.io.tmpdir"))
  props.setProperty("javax.servlet.context.tempdir", System.getProperty("java.io.tmpdir"))

  var webapps: WebAppCollection = null
  var engine: ServletEngine = null

  def addServlet(servlet: Class[_ <: HttpServlet], route: String, properties: Properties) {
    configBuilder.addServlet(servlet, route, properties)
  }

  def addServlet(servlet: Class[_ <: HttpServlet], route: String) {
    addServlet(servlet, route, new Properties())
  }

  def addServlet(servlet: HttpServlet, route: String) {
    addServlet(servlet.getClass.asInstanceOf[HttpServlet], route)
  }

  def addFilter(filter: Class[_ <: Filter], route: String) {
    configBuilder.addFilter(filter, route)
  }

  def addFilter(filter: Filter, route: String) {
    addFilter(filter.getClass.asInstanceOf[Filter], route)
  }

  def join() {
    engine.awaitInitialization(10 * 1000)
  }


  def start() {
    webapps = WebAppCollectionFactory.createWebAppCollectionWithOneContext(props, configBuilder.getConfiguration())
    webapps.startAll()
    engine = ServletEngineImpl.create(port, max_threads, webapps)
    engine.run()
  }

  def stop() {
    // FIXME: make this timeout configurable.
    engine.quit(10 * 1000) // quit with a timeout of 10 seconds.
  }
}
