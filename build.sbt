name := "fourfinance-demo"

version := "1.0.0"

lazy val `fourfinance-demo` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val macwireVersion = "2.1.0"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "com.softwaremill.macwire" %% "macros" % macwireVersion,
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

routesGenerator := InjectedRoutesGenerator