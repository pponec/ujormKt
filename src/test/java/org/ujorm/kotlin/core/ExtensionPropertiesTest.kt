package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import java.time.LocalDateTime
import kotlin.reflect.KProperty2
import kotlin.reflect.full.*

class ExtensionPropertiesTest {
    class DummyClass{}
    val DummyClass.name get() = "John"
    val DummyClass.date by lazy { LocalDateTime.now() }

    @Test
    fun testExtensionProperties() {
        val dummy = DummyClass()
        expect(dummy.name).toEqual("John")

        val propertyNames = ExtensionPropertiesTest::class.memberExtensionProperties
            .map { it.name }
            .sortedBy { it }
            .toList()

        expect(propertyNames).toHaveSize(2)
        expect(propertyNames.first()).toEqual("date")
        expect(propertyNames.last()).toEqual("name")

        val nameProperty = ExtensionPropertiesTest::class
            .memberExtensionProperties
            .filter { it.name == "name" }
            .first() as KProperty2<ExtensionPropertiesTest, DummyClass, String>

        val john : String = nameProperty.get(this, dummy)
        expect(john).toEqual("John")

        val dateProperty = ExtensionPropertiesTest::class
            .memberExtensionProperties
            .filter { it.name == "date" }
            .first() as KProperty2<ExtensionPropertiesTest, DummyClass, LocalDateTime>

        val date1 : LocalDateTime = dateProperty.get(this, dummy)
        Thread.sleep(1)
        val date2 : LocalDateTime = dateProperty.get(this, dummy)
        expect(date1).toEqual(date2)
    }
}