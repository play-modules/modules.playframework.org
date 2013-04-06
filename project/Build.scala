import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "module-repo"
  val appVersion = "1.0-SNAPSHOT"


  val appDependencies = Seq(
    "securesocial" %% "securesocial" % "2.0.12",
    "be.objectify" %% "deadbolt-2" % "1.1.2",
    "rome" % "rome" % "1.0",
    "org.markdownj" % "markdownj" % "0.3.0-1.0.2b4",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Scala Tools", url("http://scala-tools.org/repo-releases/"))(Resolver.ivyStylePatterns)
  )
}
