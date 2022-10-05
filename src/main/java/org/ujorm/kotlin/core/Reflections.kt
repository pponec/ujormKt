package org.ujorm.kotlin.core

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Stream
import kotlin.reflect.KClass

/** Source : https://www.baeldung.com/java-find-all-classes-in-package#1-system-class-loader */
class Reflections {

    fun findMemberExtensionObjectOfPackage(packageClass: KClass<*>): Stream<EntityModel<*>> {
        return findAllClassesOfPackage(packageClass)
            .filter{ it.simpleName.endsWith("Kt")}
            .map { getObjects(it) }
            .flatMap { it }
    }

    fun getObjects(clazz : Class<*>) : Stream<EntityModel<*>> {
        val domain = clazz.getDeclaredConstructor().newInstance()
        return clazz.methods
            .filter { it.parameterCount == 0 }
            .filter { it.returnType.isAssignableFrom(EntityModel::class.java) }
            .map { it.invoke(domain) }
            .stream() as Stream<EntityModel<*>>
    }

    fun findAllClassesOfPackage(packageClass: KClass<*>): Stream<Class<*>> {
        return findAllClassesOfPackage(packageClass.java.packageName)
    }

    fun findAllClassesOfPackage(packageName: String): Stream<Class<*>> {
        val stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(packageName.replace("[.]".toRegex(), "/"))
        val reader = BufferedReader(InputStreamReader(stream))
        return reader
            .lines()
            .filter { it.endsWith(".class") }
            .map { getClass(it, packageName) }
    }

    private fun getClass(className: String, packageName: String): Class<*> {
        val className = className.substring(0, className.lastIndexOf('.'))
        return Class.forName("$packageName.$className")
    }
}