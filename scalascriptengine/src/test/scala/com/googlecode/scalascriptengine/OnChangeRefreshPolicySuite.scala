package com.googlecode.scalascriptengine
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.io.File
import scalascriptengine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 25 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class OnChangeRefreshPolicySuite extends FunSuite with ShouldMatchers {

	val sourceDir = new File("testfiles/CompilationSuite")

	test("code modifications are refreshed but control returns immediatelly") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
		sse.deleteAllClassesInOutputDirectory
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.refresh
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		// this should trigger a refresh but on the background
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		// this will trigger the refresh which will occur on a different thread
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		Thread.sleep(2000)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.versionNumber should be === 2
	}

	test("code modifications are refreshed but control returns immediatelly even on errors") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefreshAsynchronously(destDir)
		sse.deleteAllClassesInOutputDirectory
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.refresh
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		copyFromSource(new File("testfiles/erroneous/ve/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		Thread.sleep(2000)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1

		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		// this will trigger the refresh which will occur on a different thread
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		Thread.sleep(2000)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.versionNumber should be === 2
	}

	test("code modifications are reloaded immediatelly") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefresh(destDir)
		sse.deleteAllClassesInOutputDirectory
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 2
		sse.versionNumber should be === 1
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.numberOfFilesChecked should be === 3
		sse.versionNumber should be === 2
	}

	test("code modifications are reloaded according to recheckEveryMillis") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefresh(destDir, 2000)
		sse.deleteAllClassesInOutputDirectory
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		sse.versionNumber should be === 1
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.numberOfFilesChecked should be === 1
		Thread.sleep(2100)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.versionNumber should be === 2
		sse.numberOfFilesChecked should be === 2
	}

	test("code modifications are reloaded according to recheckEveryMillis even when errors") {
		val destDir = newTmpDir("dynamicsrc")
		val sse = ScalaScriptEngine.onChangeRefresh(destDir, 2000)
		sse.deleteAllClassesInOutputDirectory
		copyFromSource(new File(sourceDir, "v1/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		copyFromSource(new File("testfiles/erroneous/ve/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		Thread.sleep(2100)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		sse.versionNumber should be === 1
		copyFromSource(new File(sourceDir, "v2/reload"), destDir)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v1"
		Thread.sleep(2100)
		sse.newInstance[TestClassTrait]("reload.Reload").result should be === "v2"
		sse.versionNumber should be === 2
	}
}