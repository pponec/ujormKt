package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.entity.Departments
import org.ujorm.kotlin.core.entity.EntityProvider

internal class ReflectionsTest {

    @Test
    fun findAllClassesOfPackage() {
        val instance = Reflections()

        val classes = instance.findAllClassesOfPackage(Departments::class)
            .filter { it.simpleName.endsWith("Kt")}
            .toList()

        expect(classes).toHaveSize(2)
    }

    /**
    java.lang.NoSuchMethodException: org.ujorm.kotlin.core.entity.DepartmentKt.<init>()

    at java.base/java.lang.Class.getConstructor0(Class.java:3585)
    at java.base/java.lang.Class.getDeclaredConstructor(Class.java:2754)
    at org.ujorm.kotlin.core.Reflections.getObjects(Reflections.kt:19)

    https://stackoverflow.com/questions/28294509/accessing-kotlin-extension-functions-from-java
     */
    @Test
    fun findMemberExtensionObjectOfPackage() {
        val provider: AbstractEntityProvider = EntityProvider
        val instance = Reflections()


        val objects = instance.findMemberExtensionObjectOfPackage(provider::class, provider)
            .toList()

        expect(objects).toHaveSize(2)
    }
}