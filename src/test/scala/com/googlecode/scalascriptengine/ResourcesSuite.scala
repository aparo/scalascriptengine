package com.googlecode.scalascriptengine

import java.io.File

import com.googlecode.scalascriptengine.scalascriptengine._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * @author kostantinos.kougios
 *
 *         29 Dec 2011
 */
class ResourcesSuite extends AnyFunSuite {
  val sourceDir = new File("testfiles/ResourcesSuite")

  val in = getClass.getResourceAsStream("version.txt")
  val src = scala.io.Source.fromInputStream(in)

  test("loads resources from classpath") {
    val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(new File(sourceDir, "v1")))
    sse.deleteAllClassesInOutputDirectory()
    sse.refresh

    val t: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
    t.result should be("v1")
  }

  test("loads changed resources from classpath without refreshing") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(destDir))
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.refresh

    val t1: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
    t1.result should be("v1")

    copyFromSource(new File(sourceDir, "v2"), destDir)
    val t2: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
    t2.result should be("v2")
  }

  test("loads changed resources from classpath with refreshing") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.withoutRefreshPolicy(SourcePath(destDir))
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.refresh

    val t1: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
    t1.result should be("v1")

    copyFromSource(new File(sourceDir, "v2"), destDir)
    sse.refresh
    val t2: TestClassTrait = sse.newInstance[TestClassTrait]("reload.Main")
    t2.result should be("v2")
  }
}
