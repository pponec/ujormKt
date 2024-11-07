/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao

import org.ujorm.tools.Check
import org.ujorm.tools.web.request.RContext
import org.ujorm.tools.web.request.URequest
import java.util.function.Function

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 * {
 * String value = Param.text(ServletRequest, "my default value");
 * }
 * enum Param implements HttpParam {
 * REGEXP,
 * TEXT;
 * @Override public String toString() {
 * return name().toLowerCase();
 * }
 * }
</pre> *
 *
 * @author Pavel Ponec
 */
interface HttpParameter : CharSequence {
    /** Returns a parameter name  */
    override fun toString(): String

    override fun length(): Int {
        return toString().length
    }

    override fun charAt(index: Int): Char {
        return toString()[index]
    }

    override fun subSequence(start: Int, end: Int): CharSequence {
        return toString().subSequence(start, end)
    }

    /** Returns a non-null default text value.
     * The standard value is an empty String, override it for a change.  */
    fun defaultValue(): String {
        return ""
    }

    /** Build a default non-null parameter name.  */
    fun buildParameterName(name: String?): String {
        return name ?: originalName().lowercase().replace('_', '-')
    }

    /** Get a raw name of the HTTP parameter.
     * The method can be called from the [.buildParameterName] method.
     * NOTE: The method was renamed from obsolete `name()` due a Kotlin compatibility.  */
    fun originalName(): String {
        try {
            return javaClass.getMethod("name").invoke(this).toString()
        } catch (e: ReflectiveOperationException) {
            throw IllegalStateException("Method 'name()' is not available", e)
        } catch (e: SecurityException) {
            throw IllegalStateException("Method 'name()' is not available", e)
        }
    }

    /** Returns the last parameter value of the request or a default value. The MAIN method  */
    /** Default value is an empty String  */
    @JvmOverloads
    fun of(request: URequest, defaultValue: String = defaultValue()): String {
        val results = request.getParameters(toString())
        val result = if (Check.hasLength(*results)) results[results.size - 1] else defaultValue
        return result ?: defaultValue
    }

    /** Returns the last parameter value of the request or a default value  */
    fun of(context: RContext, defaultValue: String): String {
        return of(context.request(), defaultValue)
    }

    /** Default value is an empty String  */
    fun of(context: RContext): String {
        return of(context.request(), defaultValue())
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Boolean): Boolean {
        return when (of(context)) {
            "true" -> true
            "false" -> false
            else -> defaultValue
        }
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Char): Char {
        val value = of(context)
        return if (value.isEmpty()) defaultValue else value[0]
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Short): Short {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            value.toShort()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Int): Int {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            value.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Long): Long {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            value.toLong()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Float): Float {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            value.toFloat()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /** Returns a parameter of the request or the default value  */
    fun of(context: RContext, defaultValue: Double): Double {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /** Returns a parameter of the request or the Enum class  */
    fun <V : Enum<V>?> of(context: RContext, defaultValue: V): V {
        val result = of(context, defaultValue.javaClass as Class<V>)
        return result ?: defaultValue
    }

    /** Returns a parameter of the request or the default value  */
    fun <V : Enum<V>?> of(context: RContext, clazz: Class<V>): V? {
        val value = of(context)
        for (item in clazz.enumConstants) {
            if (item.name == value) {
                return item as V
            }
        }
        return null
    }

    /** Returns a parameter of the request or the default value  */
    fun <V> of(context: RContext, defaultValue: V, decoder: Function<String?, V>): V {
        val value = of(context, EMPTY_VALUE)
        return if (value.isEmpty()) {
            defaultValue
        } else try {
            decoder.apply(value)
        } catch (e: RuntimeException) {
            defaultValue
        }
    }

    companion object {
        /** Create a default implementation  */
        fun of(name: String): HttpParameter {
            return DefaultHttpParam(name, EMPTY_VALUE)
        }

        /** Create a default implementation  */
        fun of(
            name: String,
            defaultValue: String
        ): HttpParameter {
            return DefaultHttpParam(name, defaultValue)
        }

        /** An empty text value  */
        const val EMPTY_VALUE: String = ""
    }
}
