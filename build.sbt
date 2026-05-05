ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "version-prerelease-milestone-rc",
    libraryDependencies ++= Seq(
      // Milestone: -M suffix. Final series was 2.8.x so 2.7.0-M1 is a permanent prerelease coordinate.
      "com.typesafe.akka"  %% "akka-actor"  % "2.7.0-M1",
      // Release Candidate: -RC suffix. Final 3.5.x series exists; RC1 coordinate is permanent.
      "org.typelevel"      %% "cats-effect" % "3.5.0-RC1",
      // Beta: -beta suffix, Java artifact (%). log4j-api 2.0-beta9 is a permanent coordinate on Maven Central.
      "org.apache.logging.log4j" % "log4j-api" % "2.0-beta9",
    ),
  )