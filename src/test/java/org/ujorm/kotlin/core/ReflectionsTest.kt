package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.reflections.*

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
     * https://stackoverflow.com/questions/28294509/accessing-kotlin-extension-functions-from-java
     */
    @Test
    fun findMemberExtensionObjectOfPackage() {
        val provider: AbstractEntityProvider = RefEntityProvider
        val instance = Reflections()

        val objects = instance.findMemberExtensionObjectOfPackage(provider::class, provider)
            .toList()

        expect(objects).toHaveSize(2)
    }
}