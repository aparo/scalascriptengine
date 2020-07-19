package com.googlecode.scalascriptengine

import java.io.File

import com.googlecode.scalascriptengine.classloading.ClassRegistry
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * @author kkougios
 */
class ClassRegistrySuite extends AnyFunSuite
{
	val sourceDir = new File("testfiles/ScalaClassLoaderSuite/v1")

	test("loads classes") {
		val registry = new ClassRegistry(getClass.getClassLoader, Set(sourceDir))
		registry.allClasses.map(_.getName).toSet should be(Set("test.TestDep", "test.TestParam", "test.Test"))
	}
}
