/*
 * Copyright 2021-2023 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.kotlin.core.impl

import org.ujorm.kotlin.core.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

/** Raw Entity data model
 * See the link: https://www.baeldung.com/java-dynamic-proxies
 * or see: https://xperti.io/blogs/java-dynamic-proxies-introduction/
 */
class RawEntity<D : Any> : InvocationHandler {
    internal val model: EntityModel<D>
    private val values: Array<Any?>
    private var changes: BitSet? = null
    var session: Session? = null

    constructor(model: EntityModel<D>) {
        this.model = model
        this.values = model.createArray()
    }

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any?>?): Any? {
        val methodName = method?.name ?: ""
        return when (methodName) {
            "toString" -> toString()
            "hashCode" -> hashCode()
            "equals" -> equals(args?.first())
            "~~", "get~~" -> this
            else -> {
                val prefixLength = 3
                if (methodName.length > prefixLength && (
                    methodName.startsWith("get") ||
                    methodName.startsWith("set"))
                ) {
                    val propertyName = "${methodName[prefixLength]
                        .lowercaseChar()}${methodName.substring(prefixLength + 1)}"
                    val property = model.utils().findProperty(propertyName) as PropertyNullableImpl<D, Any>
                    if (args.isNullOrEmpty()) {
                        return get(property)
                    } else {
                        set(property.data(), args.first())
                        return Unit
                    }
                } else {
                    if (methodName.length == prefixLength) {
                        if (args?.size == 1 && methodName == "get") {
                            val p = args[0] as PropertyNullable<D, Any>
                            return p.get(proxy as D)
                        }
                        if (args?.size == 2 && methodName == "set") {
                            val p = args[0] as PropertyNullable<D, Any>
                            return p.set(proxy as D, args[1])
                        }
                    }
                    if (method?.isDefault ?: false) {
                        val arguments = args ?: emptyArray()
                        return InvocationHandler.invokeDefault(proxy, method, *arguments)
                    }
                    val msg = "Method is not imlemented:" +
                            " '${model.utils().entityClass.simpleName}.$methodName()'"
                    TODO(msg)
                }
            }
        }
    }

    protected fun get(name: String): Any? = get(model.utils().findProperty(name))

    /** Get nullable value by a direct access. */
    @Deprecated("Use the method with argument type of PropertyMetadata rather.")
    internal operator fun <V : Any> get(property: PropertyNullableImpl<D, V>) : V? =
        get(property.data())

    /** Get nullable value by a direct access. */
    operator fun <V : Any> get(propertyData: PropertyMetadata<D, V>) : V? =
        values[propertyData.indexToInt()] as V?

    /** Set a nullable value */
    operator fun <V : Any> set(propertyData: PropertyMetadata<D, V>, value: V?) {
        if (session != null) {
            if (changes == null) changes = BitSet(model.utils().size)
            changes!!.set(1)
        }
        values[propertyData.indexToInt()] = value
    }

    fun <V : Any> isChanged(property: PropertyNullable<D, V>) =
        changes?.get(property.data().indexToInt()) ?: false

    fun <V : Any> isNull(property: PropertyNullable<D, V>) : Boolean =
        values[property.data().indexToInt()] == null

    override fun toString() = toString(40)

    protected fun toString(itemValueMaxLength : Int, maxDepth: Int = 3): String {
        val result = StringBuilder().append(model.utils().entityClass.simpleName)
        model.utils().properties.forEachIndexed { i, property ->
            result.append(if (i == 0) "{" else ", ")
            result.append(property.name())
            result.append('=')
            val collection = property.data().isTypeOf(List::class)
            val value = if (collection) null else values[i]
            if (value is RawEntity<*>) {
                if (maxDepth > 0) {
                    result.append(value.toString(itemValueMaxLength, maxDepth - 1))
                } else {
                    result.append("...")
                }
            } else {
                val separator = if (value is CharSequence) "\"" else ""
                val text = if (collection) "?" else  "$value"
                result.append(separator)
                result.append(
                    if (text.length <= itemValueMaxLength) text
                    else "${text.substring(0, itemValueMaxLength - 4)}...}"
                )
                result.append(separator)
            }
        }
        return result.append('}').toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!(other is AbstractEntity<*>)) return false
        val raw2 = other.`~~`()
        if (model.javaClass != raw2.model.javaClass) return false
        if (!values.contentEquals(raw2.values)) return false
        return true
    }

    /** Hash code of the values */
    override fun hashCode() = values.contentHashCode()
}

open class TempModel : EntityModel<DbRecord>(DbRecord::class)