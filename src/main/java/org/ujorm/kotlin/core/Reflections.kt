package org.ujorm.kotlin.core

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Modifier
import java.util.stream.Stream
import kotlin.reflect.KClass

/** Source : https://www.baeldung.com/java-find-all-classes-in-package#1-system-class-loader */
class Reflections {

    /** Returns instances of Entity models */
    fun findMemberExtensionObjectOfPackage(packageClass: KClass<*>, provider : AbstractEntityProvider): Stream<EntityModel<*>> {
        return findMemberExtensionObjectOfPackage(packageClass::class.java.packageName, provider)
    }

    /** Returns instances of Entity models */
    fun findMemberExtensionObjectOfPackage(packageName: String, provider : AbstractEntityProvider): Stream<EntityModel<*>> {
        return findAllClassesOfPackage(packageName)
            .filter{ it.simpleName.endsWith("Kt")}
            .map { findEntityModels(it, provider) }
            .flatMap { it.stream() }
    }

    fun findEntityModels(clazz : Class<*>, provider : AbstractEntityProvider) : List<EntityModel<*>> {
        return clazz.methods
            .filter { it.parameterCount == 1 }
            .filter { Modifier.isStatic(it.getModifiers()) }
            .filter { EntityModel::class.java.isAssignableFrom(it.returnType) }
            .map { it.invoke(null, provider) as EntityModel<*> }
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