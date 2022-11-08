package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.reflections.*

internal class ReflectionsTest {

    @Test
    fun findAllClassesOfPackage() {
        val instance = Reflections(EntityModel::class)

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
        val provider = RefEntityProvider
        val instance = Reflections(EntityModel::class)

        val objects = instance.findMemberExtensionObjectOfPackage(provider::class, provider)
            .toList()
            .sortedBy { it::class.java.simpleName }

        expect(objects).toHaveSize(2)
        expect(objects.first()).toBeTheInstance( provider.departments)
        expect(objects.last()).toBeTheInstance( provider.employees )
    }
}