import sbt.Tests.{ Group, SubProcess }

name := "scalascriptengine"

organization := "io.megl"

version := "1.3.12"

pomIncludeRepository := { _ =>
  false
}

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/kostaskougios/scalascriptengine"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/kostaskougios/scalascriptengine"),
    "scm:https://github.com/kostaskougios/scalascriptengine.git"
  )
)

developers := List(
  Developer(
    id = "alberto.paro@gmail.com",
    name = "Alberto Paro",
    email = "alberto.paro@gmail.com",
    url = url("https://github.com/aparo")
  ),
  Developer(
    id = "kostas.kougios@googlemail.com",
    name = "Konstantinos Kougios",
    email = "kostas.kougios@googlemail.com",
    url = url("https://github.com/kostaskougios")
  )
)

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.7" % Test,
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.2.0" % Test,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value
)

// fork in test cause there are conflicts with sbt classpath
def forkedJvmPerTest(testDefs: Seq[TestDefinition]) = testDefs
  .groupBy(
    test =>
      test.name match {
        case "com.googlecode.scalascriptengine.SandboxSuite" =>
          test.name
        case _ => "global"
      }
  )
  .map {
    case (name, tests) =>
      Group(
        name = name,
        tests = tests,
        runPolicy = SubProcess(ForkOptions())
      )
  }
  .toSeq

//definedTests in Test returns all of the tests (that are by default under src/test/scala).
// testGrouping in Test <<= (definedTests in Test) map forkedJvmPerTest
testGrouping in Test := { (definedTests in Test).map(forkedJvmPerTest) }.value

testOptions in Test += Tests.Argument("-oF")
