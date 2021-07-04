/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.kotlin

import kotlin.reflect.KClass

interface Operator

interface Criterion<D : Any, out OP : Operator, out V : Any> {
    val domainClass : KClass<D>
    val operator: OP
    fun eval(domain : D) : Boolean
    fun not() = BinaryCriterion(this, BinaryOperator.NOT, this)
    /** Plain text expression */
    operator fun invoke(): String
    /** Extended text expression */
    override fun toString(): String
}

interface Key<D : Any, V : Any> : CharSequence {
    val name : String
    val domainClass : KClass<D>
    val valueClass : KClass<out V>

    /** Get a value from the domain object */
    fun of(domain : D) : V

    /** Set a value to the domain object */
    fun set(domain: D, value: V) : Unit

    fun operate(operator : ValueOperator, value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    /** key() */
    operator fun invoke(): String = name

    /** Value operator */
    infix fun EQ(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.EQ, value)
    }

    /** Value operator */
    infix fun GT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.GT, value)
    }

    /** Value operator */
    infix fun LT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.LT, value)
    }
}

open class KeyImpl<D : Any, V : Any> : Key<D, V> {

    override val name: String
    override val domainClass: KClass<D>
    override val valueClass: KClass<V>
    // var type : KType = typeOf<Int?>() // TODO: how to check nullable values?
    private val setter: (D, V) -> Unit
    private val getter: (D) -> V

    constructor(name: String, domainClass: KClass<D>, valueClass: KClass<V>, setter: (D, V) -> Unit, getter: (D) -> V) {
        this.name = name
        this.domainClass = domainClass
        this.valueClass = valueClass
        this.setter = setter
        this.getter = getter
    }

    /** Get a value from the domain object */
    override fun of(domain: D): V = getter(domain)

    /** Set a value to the domain object */
    override fun set(domain: D, value: V) = setter(domain, value)

    /** For a CharSequence implementation */
    override val length: Int get() = name.length

    /** For a CharSequence implementation */
    override fun get(index: Int): Char = name[index]

    /** For a CharSequence implementation */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)

    /** For a CharSequence implementation */
    override fun toString(): String = name
}

enum class ValueOperator : Operator {
    EQ,
    LT,
    GT,
    LTE,
    GTE,
    ALL,
    NONE
}

enum class BinaryOperator : Operator {
    AND,
    OR,
    NOT,
    AND_NOT,
    OR_NOT;
}

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any>> {
    val left : Criterion<D, Operator, out Any>
    val right : Criterion<D, Operator, out Any>
    override val operator: BinaryOperator
    override val domainClass: KClass<D> get() = left.domainClass

    constructor(
        left: Criterion<D, out Operator, out Any>,
        operator: BinaryOperator,
        right: Criterion<D, out Operator, out Any>
    ) {
        this.left = left
        this.operator = operator
        this.right = right
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            BinaryOperator.AND -> left.eval(domain) && right.eval(domain)
            BinaryOperator.OR -> left.eval(domain) || right.eval(domain)
            BinaryOperator.AND_NOT -> left.eval(domain) && !right.eval(domain)
            BinaryOperator.OR_NOT -> left.eval(domain) || !right.eval(domain)
            else -> {
                throw UnsupportedOperationException("Unsupported operator: $operator")
            }
        }
    }

    /** Plain text expression */
    override operator fun invoke(): String {
        return "($left) $operator ($right))"
    }

    /** Extenced text expression */
    override fun toString(): String {
        return "${domainClass}(${this()})"
    }
}

open class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val key : Key<D, out V>
    val value : V
    override val operator: ValueOperator
    override val domainClass: KClass<D> get() = key.domainClass

    constructor(key: Key<D, out V>, operator: ValueOperator, value: V) {
        this.key = key
        this.operator = operator
        this.value = value
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            ValueOperator.ALL -> true
            ValueOperator.NONE -> false
            ValueOperator.EQ -> key.of(domain) == value
            ValueOperator.GT ->  compare(key.of(domain), value) > 0
            ValueOperator.GTE -> compare(key.of(domain), value) >= 0
            ValueOperator.LT -> compare(key.of(domain), value) < 0
            ValueOperator.LTE -> compare(key.of(domain), value) <= 0
            else -> throw java.lang.UnsupportedOperationException("Unsupported operator $operator")
        }
    }

    /** Private comparator */
    private fun <T : Any> compare(a: T?, b: T?): Int {
        if (a === b) return 0
        if (a == null) return -1
        if (b == null) return 1

        return if (a is Comparable<*>) {
            //@Suppress("UNCHECKED_CAST")
            (a as Comparable<T>).compareTo(b)
        } else {
            throw IllegalStateException("Unsupported comparation for ${this.key.valueClass}" )
        }
    }

    infix fun AND(crn: Criterion<D, out Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND, crn)
    }
    infix fun OR (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR, crn)
    }
    infix fun AND_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND_NOT, crn)
    }
    infix fun OR_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR_NOT, crn)
    }

    override operator fun invoke(): String {
        val separator = stringValueSeparator()
        return "$key $operator $separator$value$separator"
    }

    override fun toString(): String {
        return "${key.domainClass.simpleName}(${this()})"
    }

    /** A separator for String values */
    private fun stringValueSeparator() : String {
        return if (value is CharSequence) "\"" else "";
    }
}

/** Interface of meta-model */
interface AbstractMetaModel {
}

/** Meta-model of the domain object will be a generated class in the feature */
interface DomainModel {
    /** Domain class */
    val _domainClass : KClass<*>
}
