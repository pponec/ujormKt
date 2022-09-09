package org.ujorm.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.core.entity.*
import java.time.LocalDate


internal abstract class AbstractTest {

    /** Helper methods */
    protected fun <V> assertEq(currentValue: V, expectedValue: V, messageSupplier: (() -> String)? = null) {
        Assertions.assertEquals(expectedValue, currentValue, messageSupplier)
    }

    /** Helper methods */
    protected fun assertTrue(condition: Boolean, message: (() -> String)? = null) {
        Assertions.assertTrue(condition, message)
    }

    /** Helper methods */
    protected fun assertFalse(condition: Boolean, message: (() -> String)? = null) {
        Assertions.assertFalse(condition, message)
    }

}