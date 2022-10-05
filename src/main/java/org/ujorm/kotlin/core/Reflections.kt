package org.ujorm.kotlin.core

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Modifier
import java.util.stream.Stream
import kotlin.reflect.KClass

/** Source : https://www.baeldung.com/java-find-all-classes-in-package#1-system-class-loader */
class Reflections {

    fun findMemberExtensionObjectOfPackage(packageClass: KClass<*>, provider : Any): Stream<EntityModel<*>> {
        return findAllClassesOfPackage(packageClass)
            .filter{ it.simpleName.endsWith("Kt")}
            .map { getObjects(it, provider) }
            .flatMap { it }
    }

    fun getObjects(clazz : Class<*>, provider : Any) : Stream<EntityModel<*>> {
        return clazz.methods
            .filter {
                Modifier.isStatic(it.getModifiers())
            }
            .filter {
                it.parameterCount == 1
            }
            .filter {
                it.returnType.isAssignableFrom(EntityModel::class.java)
            }
            .map {
                it.invoke(provider)
            }
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