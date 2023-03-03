package org.ujorm.kotlin.orm.proxy

import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import java.io.Serializable

/**
 * Original author: https://xperti.io/blogs/java-dynamic-proxies-introduction/
 */
class ProxyDemoKt {
    @Test
    fun testProxyDemo() {
        val myImplementation = Implementation()
        val myHandler = MyHandler(myImplementation)
        val a = Proxy.newProxyInstance(
            Ifce::class.java.classLoader,
            arrayOf<Class<*>>(Ifce::class.java, Serializable::class.java),
            myHandler
        ) as Ifce
        val result = a.sendName("Joe")

        expect(result).toEqual("Hello Joe!")
        expect(result is Serializable).toEqual(true)
    }

    internal interface Ifce {
        fun sendName(str: String): String
    }

    internal class Implementation : Ifce {
        override fun sendName(name: String): String {
            return "Hello $name!"
        }
    }

    internal class MyHandler(private val original: Implementation) : InvocationHandler {
        @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
            val argument = if (args.size > 0) args[0] else ""
            println("Before the proxy: %s(\"%s\")".format(argument, method.name))
            val result = method.invoke(original, *args)
            println("After the proxy: ")
            return result
        }
    }
}