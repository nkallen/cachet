import sbt._
import Process._
import com.twitter.sbt._

class CachetProject(info: ProjectInfo) extends StandardProject(info) {
  val specs = "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test"
  val configgy = "net.lag" % "configgy" % "1.5"
  val jetty = "org.mortbay.jetty" % "jetty" % "6.1.24"
  val jetty_util = "org.mortbay.jetty" % "jetty-util" % "6.1.24"
  val jetty_client = "org.mortbay.jetty" % "jetty-client" % "6.1.24"
  val jetty_servlet_tester = "org.mortbay.jetty" % "jetty-servlet-tester" % "6.1.24"
  val jetty_sslengine = "org.mortbay.jetty" % "jetty-sslengine" % "6.1.24"
  val servlet_api = "javax.servlet" % "servlet-api" % "2.5"
  val ehcache = "net.sf.ehcache" % "ehcache" % "1.5.0"
  val asm       = "asm" % "asm" %  "1.5.3"
  val cglib     = "cglib" % "cglib" % "2.1_3"
  val hamcrest  = "org.hamcrest" % "hamcrest-all" % "1.1"
  val jmock     = "org.jmock" % "jmock" % "2.4.0"
  val objenesis = "org.objenesis" % "objenesis" % "1.1"
  val ostrich = "com.twitter" % "ostrich" % "1.1.16"

}
