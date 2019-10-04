name := "giorgia_fiscaletti_hw1"

version := "0.1"

scalaVersion := "2.13.0"

scalaSource in Compile := baseDirectory.value / "src/main/scala"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "com.hazelcast" % "hazelcast" % "3.3.3"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "junit" % "junit" % "4.13-beta-1"

logBuffered in Test := false