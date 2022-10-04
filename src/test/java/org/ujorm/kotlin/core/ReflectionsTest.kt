package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.entity.Departments

internal class ReflectionsTest {

    @Test
    fun findAllClassesOfPackage() {
        val instance = Reflections()

        val classes = instance.findAllClassesOfPackage(Departments::class)
            .filter { it.simpleName.endsWith("Kt")}
            .toList()

        expect(classes).toHaveSize(2)
    }
}