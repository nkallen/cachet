import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val twitterNet = "com.twitter" at "http://www.lag.net/nest"
  val defaultProject = "com.twitter" % "standard-project" % "0.5.9"
}
