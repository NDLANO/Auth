import java.util.Properties

val Scalaversion = "2.11.8"
val Scalatraversion = "2.4.1"
val Jettyversion = "9.2.10.v20150310"
val AwsSdkversion = "1.10.26"
val ScalaTestVersion = "2.2.6"
val MockitoVersion = "1.10.19"
val SlickVersion = "3.0.0"
val Json4sVersion = "3.3.0"
val ScalaLoggingVersion = "3.1.0"
val Log4JVersion = "2.6"
val JacksonVersion = "2.7.4"

val appProperties = settingKey[Properties]("The application properties")

appProperties := {
  val prop = new Properties()
  IO.load(prop, new File("build.properties"))
  prop
}

lazy val commonSettings = Seq(
  organization := appProperties.value.getProperty("NDLAOrganization"),
  version := appProperties.value.getProperty("NDLAComponentVersion"),
  scalaVersion := Scalaversion
)

lazy val auth = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "auth",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7"),
    libraryDependencies ++= Seq(
      "ndla" %% "network" % "0.4",
      "org.scalatra" %% "scalatra" % Scalatraversion,
      "org.scalatra" %% "scalatra-specs2" % Scalatraversion % "test",
      "org.scalatra" %% "scalatra-json" % Scalatraversion,
      "org.scalatra" %% "scalatra-swagger" % Scalatraversion,
      "org.scalatra" %% "scalatra-scalatest" % Scalatraversion % "test",
      "org.scalatest" % "scalatest_2.11" % ScalaTestVersion % "test",
      "org.scalaj" %% "scalaj-http" % "2.0.0",
      "org.json4s" %% "json4s-native" % Json4sVersion,
      "org.json4s" %% "json4s-ext" % Json4sVersion,
      "org.json4s"   %% "json4s-jackson" % Json4sVersion,
      "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "container;compile",
      "org.eclipse.jetty" % "jetty-plus" % Jettyversion % "container",
      "com.github.scribejava" % "scribejava-apis" % "2.1.0",
      "org.mockito" % "mockito-all" % MockitoVersion % "test",
      "com.netaporter" %% "scala-uri" % "0.4.13",
      "org.scalikejdbc" %% "scalikejdbc" % "2.2.8",
      "org.scalikejdbc" %% "scalikejdbc-test" % "2.2.8" % "test",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
      "com.h2database" % "h2" % "1.4.191",
      "org.flywaydb" % "flyway-core" % "4.0",
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "org.apache.logging.log4j" % "log4j-api" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-core" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4JVersion,
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test",
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % JacksonVersion
    )
  ).enablePlugins(DockerPlugin).enablePlugins(GitVersioning).enablePlugins(JettyPlugin)

assemblyJarName in assembly := "auth.jar"
mainClass in assembly := Some("no.ndla.auth.JettyLauncher")
assemblyMergeStrategy in assembly := {
  case "mime.types" => MergeStrategy.filterDistinctLines
  case PathList("org", "joda", "convert", "ToString.class") => MergeStrategy.first
  case PathList("org", "joda", "convert", "FromString.class") => MergeStrategy.first
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// Make the docker task depend on the assembly task, which generates a fat JAR file
docker <<= (docker dependsOn assembly)

dockerfile in docker := {
  val artifact = (assemblyOutputPath in assembly).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("java")

    add(artifact, artifactTargetPath)
    entryPoint("java", "-Dorg.scalatra.environment=production", "-jar", artifactTargetPath)
  }
}

val gitHeadCommitSha = settingKey[String]("current git commit SHA")
gitHeadCommitSha in ThisBuild := Process("git log --pretty=format:%h -n 1").lines.head

imageNames in docker := Seq(
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some(System.getProperty("docker.tag", "SNAPSHOT")))
)

parallelExecution in Test := false
