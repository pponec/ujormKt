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
import org.ujorm.kotlin.core.impl.Constants.CLOSED_MESSAGE
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties

class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>> {
    val left: Criterion<D, Operator, Any?>
    val right: Criterion<D, Operator, Any?>
    override val operator: BinaryOperator
    override val entityClass: KClass<D> get() = left.entityClass

    constructor(
        left: Criterion<D, Operator, Any?>,
        operator: BinaryOperator,
        right: Criterion<D, Operator, Any?>
    ) {
        this.left = left
        this.operator = operator
        this.right = right
    }

    override fun evaluate(entity: D): Boolean {
        return when (operator) {
            BinaryOperator.AND -> left(entity) && right(entity)
            BinaryOperator.OR -> left(entity) || right(entity)
            BinaryOperator.NOT -> !left(entity)
            BinaryOperator.AND_NOT -> left(entity) && !right(entity)
            BinaryOperator.OR_NOT -> left(entity) || !right(entity)
            else -> {
                TODO("Unsupported operator: ${operator.name}")
            }
        }
    }

    /** Plain text expression */
    override operator fun invoke(): String {
        val brackets = right.operator !== BinaryOperator.AND
        val lBracket = if (brackets) "(" else ""
        val rBracket = if (brackets) ")" else ""
        return when (operator) {
            BinaryOperator.NOT -> /**/ "${operator.name} $lBracket${right.invoke()}$rBracket"
            else -> "(${left.invoke()}) ${operator.name} $lBracket${right.invoke()}$rBracket"
        }
    }

    /** Extended text expression */
    override fun toString(): String {
        return "${entityClass.simpleName}: ${invoke()}"
    }
}

class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val property: PropertyNullable<D, out V>
    val value: V?
    override val operator: ValueOperator
    override val entityClass: KClass<D> get() = property.data().entityClass

    constructor(property: PropertyNullable<D, out V>, operator: ValueOperator, value: V?) {
        this.property = property
        this.operator = operator
        this.value = value
    }

    override fun evaluate(entity: D): Boolean =
        operator.evaluate(entity, property, value)

    /** Private comparator */
    private fun <T : Any> compare(a: T?, b: T?): Int {
        if (a === b) return 0
        if (a == null) return -1
        if (b == null) return 1

        return if (a is Comparable<*>) {
            (a as Comparable<T?>).compareTo(b)
        } else {
            throw IllegalStateException("Unsupported comparation for ${property.info()}")
        }
    }

    override operator fun invoke(): String {
        val separator = stringValueSeparator()
        return "${property.name()} ${operator.name} $separator$value$separator"
    }

    override fun toString(): String {
        return "${property.data().entityClass.simpleName}: ${invoke()}"
    }

    /** A separator for String values */
    private fun stringValueSeparator(): String {
        return if (value is CharSequence) "\"" else ""
    }
}
