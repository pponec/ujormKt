package org.ujorm.kotlin.demo

import org.junit.jupiter.api.Test
import org.ujorm.kotlin.model.User
import kotlin.reflect.KClass
import org.ujorm.kotlin.demo.main as main1

internal class DemoKtTest {

    @Test
    fun mainTest() {
        main1()
    }

    @Test
    fun classTest() {
        var xxx : KClass<User> = User::class;
        assert(xxx != null)
    }
}