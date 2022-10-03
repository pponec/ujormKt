package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test
import java.time.LocalDate
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import kotlin.reflect.KProperty2
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

        val propertyNames = ExtensionPropertiesTest::class.memberExtensionProperties
            .map { it.name }
            .sortedBy { it }
            .toList()

        expect(propertyNames).toHaveSize(2)
        expect(propertyNames.first()).toEqual("date")
        expect(propertyNames.last()).toEqual("name")

        val property = ExtensionPropertiesTest::class
            .memberExtensionProperties
            .filter { it.name == "name" }
            .first() as KProperty2<ExtensionPropertiesTest, DummyClass, String>

        val dummy = DummyClass()
        val john = property.get(this, dummy) as String
        expect(john).toEqual("John")
    }
}