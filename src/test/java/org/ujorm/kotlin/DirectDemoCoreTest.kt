package org.ujorm.kotlin

import org.junit.jupiter.api.Test
import org.ujorm.kotlin.model.direct.*
import kotlin.reflect.KClass

internal class DirectDemoCoreTest {

    @Test
    fun mainTest() = main()

    @Test
    fun classTest() {
        var xxx : KClass<Employee> = Employee::class
        assert(xxx != null)
    }
}