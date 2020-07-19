package com.googlecode.scalascriptengine

import java.io.File
import java.security.AccessControlException

import com.googlecode.scalascriptengine.classloading.ClassLoaderConfig
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * @author konstantinos.kougios
 *
 *         7 Oct 2012
 */
class SandboxSuite extends AnyFunSuite with BeforeAndAfterAll {
  val sourceDir = new File("testfiles/SandboxSuite")
  val config = ScalaScriptEngine.defaultConfig(sourceDir).copy(
    classLoaderConfig = ClassLoaderConfig.Default.copy(
      protectPackages = Set("javax.swing"),
      protectClasses = Set("java.lang.Thread") // note: still threads can be created via i.e. Executors
    )
  )
  System.setProperty("script.classes", config.targetDirs.head.toURI.toString)

  val policy = new File("testfiles/SandboxSuite/test.policy")
  System.setProperty("java.security.policy", policy.toURI.toString)
  val sseSM = new SSESecurityManager(new SecurityManager)
  System.setSecurityManager(sseSM)
  System.getSecurityManager should be theSameInstanceAs sseSM

  val sse = ScalaScriptEngine.onChangeRefresh(config, 5)
  sse.deleteAllClassesInOutputDirectory()
  sse.refresh

  test("will prevent access of a package") {
    val ex = intercept[AccessControlException] {
      sse.newInstance[TestClassTrait]("test.TryPackage").result
    }
    ex.getMessage should be("access to class javax.swing.Icon not allowed")
  }

  test("will prevent creating a thread") {
    val ex = intercept[AccessControlException] {
      sse.newInstance[TestClassTrait]("test.TryThread").result
    }
    ex.getMessage should be("access to class java.lang.Thread not allowed")
  }

  test("will prevent access to a file") {
    val ex = intercept[AccessControlException] {
      sseSM.secured {
        val tct = sse.newInstance[TestClassTrait]("test.TryFile")
        tct.result should be("directory")
      }
    }
    ex.getPermission match {
      case fp: java.io.FilePermission if fp.getActions == "read" && fp.getName == "/tmp" =>
      // ok
      case _ => throw ex
    }
  }

  test("will allow access to a file") {
    sseSM.secured {
      val tct = sse.newInstance[TestClassTrait]("test.TryHome")
      tct.result should be("directory")
    }
  }

  test("sandbox eval") {
    intercept[AccessControlException] {
      val ect = EvalCode.with1Arg[String, String]("s", "s+classOf[java.lang.Thread].getName", config.classLoaderConfig)
      val f = ect.newInstance
      f("hi")
    }
  }

  override def afterAll =
    System.setSecurityManager(null)
}
