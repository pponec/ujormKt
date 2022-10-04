package org.ujorm.kotlin.core

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

/** Source : https://www.baeldung.com/java-find-all-classes-in-package#1-system-class-loader */
class Reflections {

    fun findAllClassesUsingClassLoader(packageName: String): Set<Class<*>> {
        val stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(packageName.replace("[.]".toRegex(), "/"))
        val reader = BufferedReader(InputStreamReader(stream))
        return reader
            .lines()
            .filter { line: String -> line.endsWith(".class") }
            .map { getClass(it, packageName) }
            .collect(Collectors.toSet())
    }

    private fun getClass(className: String, packageName: String): Class<*> {
        val className = className.substring(0, className.lastIndexOf('.'))
        return Class.forName("$packageName.$className")
    }
}