import scala.sys.process._

lazy val is_gpu = System.getProperty("is_gpu", "false").equals("true")
lazy val is_m1 = System.getProperty("is_m1", "false").equals("true")
lazy val is_aarch64 = System.getProperty("is_aarch64", "false").equals("true")

lazy val supportedScalaVersions = List(scala212)
lazy val scala212 = "2.12.15"

name := {
  if (is_gpu)
    "jsl-llamacpp-gpu"
  else if (is_m1)
    "jsl-llamacpp-m1"
  else if (is_aarch64)
    "jsl-llamacpp-aarch64"
  else
    "jsl-llamacpp-cpu"
}

organization := "com.johnsnowlabs.nlp"
version := "0.1.0-rc1-SNAPSHOT"
scalaVersion := scala212

// Java Configuration
Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")

// Java Specific Configuration
autoScalaLibrary := false // exclude scala-library from dependencies

licenses += "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")
(ThisBuild / resolvers) += "Another Maven" at "https://mvnrepository.com/artifact/"

// For Debugging Purposes
//logLevel := Level.Debug

// Note: Maven needs to be available
// Compile Tasks
val compileLlamaCpp = taskKey[Unit]("Compile llama.cpp")
compileLlamaCpp := {
  val log = streams.value.log
  val cmakeFlags = if (is_gpu) "-DLLAMA_CUDA=ON" else "" // Additional flags for llama.cpp
  // Run the cmake commands
  val compileExitCode =
    s"mvn compile" #&& "mkdir -p build" #&& s"cmake -B build $cmakeFlags" #&& "cmake --build build --config Release" // Adjust this to your specific CMake command

  if (compileExitCode.! != 0) {
    throw new Exception(s"llama.cpp build: CMake command failed with exit code $compileExitCode")
  }
}

// Compile and package commands in sbt depend on llama.cpp compilation
(Compile / compile) := ((Compile / compile) dependsOn compileLlamaCpp).value
(Compile / packageBin) := ((Compile / packageBin) dependsOn Compile / compile).value

// Maven Tests
val testLlamaCpp = taskKey[Unit]("Test llama.cpp")

testLlamaCpp := {
  val log = streams.value.log
  val testExitCode = s"mvn test".!
  if (testExitCode != 0) {
    throw new Exception(s"mvn test: Maven test failed with exit code $testExitCode")
  }
}
// Test commands in sbt depend on llama.cpp tests
(Test / test) := testLlamaCpp.value

// Maven Clean
val mavenClean = taskKey[Unit]("Clean maven and cmake")
mavenClean := {
  val log = streams.value.log
  val mvnCleanExitCode = s"mvn clean".!
  val buildCleanExitCode = s"rm -rf build".!
  val cleanLibsExitCode = s"rm -rf src/main/resources/com/johnsnowlabs/nlp/llama/".!
  if (mvnCleanExitCode != 0 || buildCleanExitCode != 0) {
    throw new Exception(s"Clean maven and cmake: Some tasks failed.")
  }
}
// Clean commands in sbt depend on llama.cpp clean
clean := (clean dependsOn mavenClean).value

lazy val mavenProps = settingKey[Unit]("workaround for Maven properties")
lazy val root = (project in file("."))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    mavenProps := {
      sys.props("javacpp.platform.extension") = if (is_gpu) "-gpu" else ""
    })

// Dependencies
libraryDependencies += "org.jetbrains" % "annotations" % "24.1.0"
// For running junit tests in sbt
libraryDependencies += "junit" % "junit" % "4.13.2" % Test
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

/** Enable for debugging */
(Test / testOptions) += Tests.Argument("-oF")

/** Publish test artifact * */
(Test / publishArtifact) := true

(ThisBuild / developers) := List(
  Developer(
    id = "DevinTDHa",
    name = "Devin Ha",
    email = "devin@johnsnowlabs.com",
    url = url("https://github.com/DevinTDHa")))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/JohnSnowLabs/jsl-llama.cpp"),
    "scm:git@github.com:JohnSnowLabs/jsl-llama.cpp.git"))

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")

sonatypeProfileName := "com.johnsnowlabs.nlp"

publishTo := sonatypePublishToBundle.value

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

sonatypeCredentialHost := "s01.oss.sonatype.org"

publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

homepage := Some(url("https://nlp.johnsnowlabs.com"))
