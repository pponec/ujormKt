/*
 * Copyright 2022-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.kotlin.core.impl

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Modifier
import java.util.stream.Stream
import kotlin.reflect.KClass

/** Source : https://www.baeldung.com/java-find-all-classes-in-package#1-system-class-loader */
class Reflections<T : Any>(
    val targetClass : KClass<T>
) {

    /** Returns instances of Entity models */
    fun findMemberExtensionObjectOfPackage(packageClass: KClass<*>, entityProvider : Any): Stream<T> {
        return findMemberExtensionObjectOfPackage(packageClass.java.packageName, entityProvider)
    }

    /** Returns instances of Entity models */
    fun findMemberExtensionObjectOfPackage(packageName: String, entityProvider : Any): Stream<T> {
        return findAllClassesOfPackage(packageName)
            .filter{ it.simpleName.endsWith("Kt")}
            .map { findEntityModels(it, entityProvider) }
            .flatMap { it.stream() }
    }

    fun findEntityModels(clazz : Class<*>, entityProvider : Any) : List<T> {
        return clazz.methods
            .filter { it.parameterCount == 1 }
            .filter { Modifier.isStatic(it.getModifiers()) }
            .filter { targetClass.java.isAssignableFrom(it.returnType) }
            .map { it.invoke(null, entityProvider) as T }
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