package org.ujorm.kotlin.proxy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.invoke.MethodHandles
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class ProxyKtDemoTest {
    @Test
    fun testProxy() {
        val entityClass: Class<*> = KDuck::class.java
        val handler = InvocationHandler { proxy, method, args ->
            if (method.isDefault) {
                val constructor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java)
                constructor.isAccessible = true
                constructor.newInstance(entityClass)
                    .`in`(entityClass)
                    .unreflectSpecial(method, entityClass)
                    .bindTo(proxy)
                    .invokeWithArguments()
            } else {
                when (method.name) {
                    "name" -> "XYZ"
                    else -> null
                }
            }
        }
        val duck = Proxy.newProxyInstance(
            entityClass.classLoader, arrayOf(entityClass), handler
        ) as KDuck
        val value = duck.quack()
        val name = duck.name()
        val age = duck.age()
        Assertions.assertEquals("QUACK", value)
        Assertions.assertEquals("XYZ", name)
        Assertions.assertEquals(null, age)
    }
}
