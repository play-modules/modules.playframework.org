import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "module-repo"
  val appVersion      = "1.0-SNAPSHOT"


  val appDependencies = Seq(
    "securesocial" % "securesocial_2.9.1" % "2.0.7",
    "be.objectify" %% "deadbolt-2" % "1.1.2",
    "rome" % "rome" % "1.0",
    "org.markdownj" % "markdownj" % "0.3.0-1.0.2b4",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings (
    resolvers += Resolver.url("SecureSocial Repository", url("http://securesocial.ws/repository/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Scala Tools", url("http://scala-tools.org/repo-releases/"))(Resolver.ivyStylePatterns)
  )
}
