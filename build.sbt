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
version := "0.1.0-SNAPSHOT"
scalaVersion := scala212

// Java Configuration
//javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")
Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")
//crossPaths := false // drop off Scala suffix from artifact names. Or keep it for now to keep it consistent with the other jsl packages
autoScalaLibrary := false // exclude scala-library from dependencies

licenses += "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")
(ThisBuild / resolvers) += "Another Maven" at "https://mvnrepository.com/artifact/"

// Compile Tasks
val compileLlamaCpp = taskKey[Unit]("Compile llama.cpp")
compileLlamaCpp := {
  val log = streams.value.log
  val cmakeFlags = if (is_gpu) "-DLLAMA_CUDA=ON" else "" // Addiitonal flags for llama.cpp
  // Run the cmake commands
  val compileExitCode =
    s"mvn clean compile" #&& "mkdir -p build" #&& s"cmake -B build $cmakeFlags" #&& "cmake --build build" // Adjust this to your specific CMake command

  if (compileExitCode.! != 0) {
    throw new Exception(s"llama.cpp build: CMake command failed with exit code $compileExitCode")
  }
}

// Jar Folders
//if (is_m1.equals("true"))
//  unmanagedBase := baseDirectory.value / "m1-jar"
//else if (is_aarch64.equals("true"))
//  unmanagedBase := baseDirectory.value / "aarch64-jar"
//else if (is_gpu.equals("true"))
//  unmanagedBase := baseDirectory.value / "gpu-jar"
//else
//  unmanagedBase := baseDirectory.value / "cpu-jar"

lazy val mavenProps = settingKey[Unit]("workaround for Maven properties")
lazy val root = (project in file("."))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    mavenProps := {
      sys.props("javacpp.platform.extension") = if (is_gpu.equals("true")) "-gpu" else ""
    })

//(assembly / assemblyJarName) := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

// Dependencies
libraryDependencies += "org.jetbrains" % "annotations" % "24.1.0"
// For running junit tests in sbt
libraryDependencies += "junit" % "junit" % "4.13.2" % Test
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

/** Enable for debugging */
(Test / testOptions) += Tests.Argument("-oF")

/** Disables tests in assembly */
//(assembly / test) := {}

/** Publish test artifact * */
(Test / publishArtifact) := true

//(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)

(ThisBuild / developers) := List(
  Developer(
    id = "maziyarpanahi",
    name = "Maziyar Panahi",
    email = "maziyar@johnsnowlabs.com",
    url = url("https://github.com/maziyarpanahi")),
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

//(Compile / assembly / artifact) := {
//  val art = (Compile / assembly / artifact).value
//  art.withClassifier(None)
//}

//addArtifact(Compile / assembly / artifact, assembly)
