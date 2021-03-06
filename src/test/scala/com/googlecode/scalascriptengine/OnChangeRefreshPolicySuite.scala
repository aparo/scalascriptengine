package com.googlecode.scalascriptengine

import java.io.File

import com.googlecode.scalascriptengine.scalascriptengine._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * @author kostantinos.kougios
 *
 *         25 Dec 2011
 */
class OnChangeRefreshPolicySuite extends AnyFunSuite {

  val sourceDir = new File("testfiles/versions")

  test("onChangeRefresh: two source/output folders") {
    val src1 = newTmpDir("dynamicsrc1")
    val src2 = newTmpDir("dynamicsrc2")
    val destDir1 = newTmpDir("out1")
    val destDir2 = newTmpDir("out2")
    val sse = ScalaScriptEngine.onChangeRefresh(
      Config(
        sourcePaths = List(
          SourcePath(Set(src1), destDir1),
          SourcePath(Set(src2), destDir2)
        )
      ),
      100
    )

    sse.deleteAllClassesInOutputDirectory()

    copyFromSource(new File("testfiles/src1"), src1)
    copyFromSource(new File("testfiles/src2"), src2)

    sse.refresh

    sse.newInstance[TestClassTrait]("test.A").result should be("A")
    sse.versionNumber should be(1)
    sse.newInstance[TestClassTrait]("test.B").result should be("B")
    sse.versionNumber should be(1)

    Thread.sleep(1000)
    copyFromSource(new File("testfiles/src3"), src1)
    sse.newInstance[TestClassTrait]("test.A").result should be("AMod")
    sse.versionNumber should be(2)
    sse.newInstance[TestClassTrait]("test.B").result should be("B")
    sse.versionNumber should be(2)
  }

  test("onChangeRefreshAsynchronously: code modifications are refreshed but control returns immediatelly") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.refresh
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    // this should trigger a refresh but on the background
    copyFromSource(new File(sourceDir, "v2"), destDir)
    // this will trigger the refresh which will occur on a different thread
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    Thread.sleep(2000)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v2")
    sse.versionNumber should be(2)
    sse.shutdown
  }

  test(
    "onChangeRefreshAsynchronously: code modifications are refreshed but control returns immediatelly even on errors"
  ) {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.refresh
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    copyFromSource(new File("testfiles/erroneous/ve"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    Thread.sleep(1000)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)

    copyFromSource(new File(sourceDir, "v2"), destDir)
    // this will trigger the refresh which will occur on a different thread
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    Thread.sleep(2000)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v2")
    sse.versionNumber should be(2)
    sse.shutdown
  }

  test("onChangeRefresh: code modifications are reloaded immediatelly") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.onChangeRefresh(destDir)
    sse.deleteAllClassesInOutputDirectory()
    for (i <- 1 to 10) {
      copyFromSource(new File(sourceDir, "v1"), destDir)
      sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
      if (i == 1) sse.numberOfTimesSourcesTestedForModifications should be(1)
      sse.versionNumber should be(i * 2 - 1)
      sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
      if (i == 1) sse.numberOfTimesSourcesTestedForModifications should be(2)
      sse.versionNumber should be(i * 2 - 1)
      Thread.sleep(10)
      copyFromSource(new File(sourceDir, "v2"), destDir)
      sse.newInstance[TestClassTrait]("reload.Reload").result should be("v2")
      if (i == 1) sse.numberOfTimesSourcesTestedForModifications should be(3)
      sse.versionNumber should be(i * 2)
    }
  }

  test("onChangeRefresh: code modifications are reloaded according to recheckEveryMillis") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.onChangeRefresh(destDir, 2000)
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.numberOfTimesSourcesTestedForModifications should be(1)
    sse.versionNumber should be(1)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.numberOfTimesSourcesTestedForModifications should be(1)
    sse.versionNumber should be(1)
    copyFromSource(new File(sourceDir, "v2"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.numberOfTimesSourcesTestedForModifications should be(1)
    Thread.sleep(2100)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v2")
    sse.versionNumber should be(2)
    sse.numberOfTimesSourcesTestedForModifications should be(2)
  }

  test("onChangeRefresh: code modifications are reloaded according to recheckEveryMillis even when errors") {
    val destDir = newTmpDir("dynamicsrc")
    val sse = ScalaScriptEngine.onChangeRefresh(destDir, 2000)
    sse.deleteAllClassesInOutputDirectory()
    copyFromSource(new File(sourceDir, "v1"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    sse.versionNumber should be(1)
    copyFromSource(new File("testfiles/erroneous/ve"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    Thread.sleep(2100)
    try {
      sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    } catch {
      case e: Throwable =>
    }
    sse.versionNumber should be(1)
    copyFromSource(new File(sourceDir, "v2"), destDir)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v1")
    Thread.sleep(3100)
    sse.newInstance[TestClassTrait]("reload.Reload").result should be("v2")
    sse.versionNumber should be(2)
  }
}
