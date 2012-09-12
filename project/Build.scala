import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "module-repo"
  val appVersion      = "1.0-SNAPSHOT"

  val ssDependencies = Seq(
    "com.typesafe" %% "play-plugins-util" % "2.0.1",
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val secureSocial = PlayProject("securesocial", appVersion, ssDependencies, mainLang = SCALA, path = file("modules/securesocial")).settings(
    resolvers ++= Seq(
      "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    )
  )

  val appDependencies = Seq(
    "be.objectify" %% "deadbolt-2" % "1.1.2",
    "rome" % "rome" % "1.0",
    "org.markdownj" % "markdownj" % "0.3.0-1.0.2b4",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings (
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Scala Tools", url("http://scala-tools.org/repo-releases/"))(Resolver.ivyStylePatterns)
  ).dependsOn(secureSocial).aggregate(secureSocial)
}
