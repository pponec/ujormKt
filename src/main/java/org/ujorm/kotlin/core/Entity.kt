/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.kotlin.core

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

interface Entity<D : Any> {

    /** Provides a RawEntity object */
    fun `___`(): RawEntity<D>
}

/** A session context */
interface Session {
}

/** Raw Entity data model
 * See the link: https://www.baeldung.com/java-dynamic-proxies
 * or see: https://xperti.io/blogs/java-dynamic-proxies-introduction/
 * */
open class RawEntity<D : Any> : InvocationHandler, Entity<D>{
    private val model: EntityModel<D>
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
            "___" -> `___`()
            else -> {
                if (methodName.length > 3 && (
                    methodName.startsWith("get") ||
                    methodName.startsWith("set"))
                ) {
                    val position = 3
                    val propertyName = "${methodName[position].lowercaseChar()}${methodName.substring(position + 1)}"
                    val property = model.utils().findProperty(propertyName) as PropertyNullable<D, Any>
                    if (args.isNullOrEmpty()) {
                        return property.get(values)
                    } else {
                        property.set(values, args.first())
                        return Unit
                    }
                } else {
                    val msg = "Method: ${model.utils().entityClass.simpleName}.$methodName()"
                    TODO(msg)
                }
            }
        }
    }

    protected fun get(name: String): Any? = get(model.utils().findProperty(name))

    /** Get value */
    fun <V : Any> get(property: PropertyNullable<D, V>) = property[values]

    /** Set value */
    fun <V : Any> set(property: PropertyNullable<D, V>, value: V) {
        if (session != null) {
            if (changes == null) changes = BitSet(model.utils().size)
            changes!!.set(1)
        }
        property[values] = value
    }

    fun <V : Any> isChanged(property: PropertyNullable<D, V>) =
        changes?.get(property.data().indexToInt()) ?: false

    override fun `___`(): RawEntity<D> = this

    override fun toString() = toString(40)

    protected fun toString(itemValueMaxLength : Int, maxDepth: Int = 3): String {
        val result = StringBuilder().append(model.utils().entityClass.simpleName)
        model.utils().properties.forEachIndexed { i, property ->
            result.append(if (i == 0) "{" else ", ")
            result.append(property.name())
            result.append('=')
            val value = values[i]
            if (value is RawEntity<*>) {
                if (maxDepth > 0) {
                    result.append(value.toString(itemValueMaxLength, maxDepth - 1))
                } else {
                    result.append("...")
                }
            } else {
                val text = "$value"
                result.append(if (text.length <= itemValueMaxLength) text
                else "${text.substring(0, itemValueMaxLength - 4)}...}")
            }
        }
        return result.append('}').toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!(other is Entity<*>)) return false
        val raw2 = other.`___`()
        if (model.javaClass != raw2.model.javaClass) return false
        if (!values.contentEquals(raw2.values)) return false
        return true
    }

    /** Hash code of the values */
    override fun hashCode() = values.contentHashCode()
}

/** Common database recored entity */
interface DbRecord : Entity<Any>

open class TempModel : EntityModel<DbRecord>(DbRecord::class)