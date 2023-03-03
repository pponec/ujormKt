package org.ujorm.kotlin.core

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class MockKSample {

    @MockK
    lateinit var service: Runnable

    @Test
    fun testService() {
        if (service == null) {
            throw IllegalStateException("Missing mock")
        }
    }
}