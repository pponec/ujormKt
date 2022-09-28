package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test
import java.time.LocalDate
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import kotlin.reflect.full.*

class ExtensionPropertiesTest {
    class DummyClass{}
    val DummyClass.name get() = "John"
    val DummyClass.date get() = LocalDate.parse("2022-09-28")

    @Test
    fun testExtensionProperties() {
        val dummyClass = DummyClass()
        expect(dummyClass.name).toEqual("John")
        expect(dummyClass.date.toString()).toEqual("2022-09-28")

        val propertyNames = DummyClass::class.memberExtensionProperties
            .stream()
            .map { it.name }
            .toList()

        expect(propertyNames).toHaveSize(2) // Real size is zero (?)
    }

    @Test
    fun testExtensionProperties2() {

        val dummyClass = DummyClass()
        expect(dummyClass.name).toEqual("John")
        expect(dummyClass.date.toString()).toEqual("2022-09-28")

        val propertyNames1 = DummyClass::class.staticProperties
            .stream()
            .map { it.name }
            .toList()

        val propertyNames2 = DummyClass::class.memberProperties
            .stream()
            .map { it.name }
            .toList()

        val propertyNames3 = DummyClass::class.memberExtensionProperties
            .stream()
            .map { it.name }
            .toList()

        val propertyNames4 = DummyClass::class.declaredMemberProperties
            .stream()
            .map { it.name }
            .toList()

        val propertyNames5 = DummyClass::class.declaredMemberExtensionProperties
            .stream()
            .map { it.name }
            .toList()

        val all = mutableListOf<String>()
        all += propertyNames1
        all += propertyNames2
        all += propertyNames3
        all += propertyNames4
        all += propertyNames5

        expect(all.size).notToEqual(0)
    }
}