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

interface Criterion<D : Any, out OP : Operator, out V : Any?> {
    val domainClass : KClass<D>
    val operator: OP
    fun eval(domain : D) : Boolean
    fun not() : Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>>
    = BinaryCriterion(this, BinaryOperator.NOT, this)
    /** Plain text expression */
    operator fun invoke(): String
    /** Extended text expression */
    override fun toString(): String

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
}

/** Property descriptor for nullable values */
interface PropertyNullable<D : Any, V : Any> : CharSequence {
    val name : String
    /** Is the value required (non-null) ? */
    val required : Boolean
    val domainClass : KClass<D>
    val valueClass : KClass<out V>

    /** Get a value from the domain object */
    fun of(domain : D) : V?

    /** Set a value to the domain object */
    fun set(domain: D, value: V?) : Unit

    fun operate(operator : ValueOperator, value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    /** Name of property */
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

/** Property descriptor for non-null values */
interface Property<D : Any, V : Any> : PropertyNullable<D, V> {
    /** Get a value from the domain object */
    override fun of(domain : D) : V

    /** Set a non-null value to the domain object */
    override fun set(domain: D, value: V?) : Unit
}

/** Abstract property descriptor */
abstract class AbstractProperty<D : Any, V : Any> : PropertyNullable<D, V> {
    override val name: String
    /** Required value (mon-nnull)
     * KType = typeOf<Int?>()  */
    override val domainClass: KClass<D>
    override val valueClass: KClass<V>

    constructor(name: String, domainClass: KClass<D>, valueClass: KClass<V>,) {
        this.name = name
        this.domainClass = domainClass
        this.valueClass = valueClass
    }

    /** For a CharSequence implementation */
    override val length: Int get() = name.length

    /** For a CharSequence implementation */
    override fun get(index: Int): Char = name[index]

    /** For a CharSequence implementation */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)

    /** For a CharSequence implementation */
    override fun toString(): String = name
}

/** Property descriptor for nullable values */
open class PropertyNullableImpl<D : Any, V : Any> : AbstractProperty<D, V> {
    override val required: Boolean get() = false
    private val setter: (D, V?) -> Unit
    private val getter: (D) -> V?

    constructor(
        name: String,
        domainClass: KClass<D>,
        valueClass: KClass<V>,
        setter: (D, V?) -> Unit,
        getter: (D) -> V?
    ) : super(name, domainClass, valueClass) {
        this.setter = setter
        this.getter = getter
    }

    override fun of(domain: D): V? = getter(domain)
    override fun set(domain: D, value: V?) = setter(domain, value)
}

/** Property for non-null values */
open class PropertyImpl<D : Any, V : Any> : AbstractProperty<D, V> , Property<D, V> {
    override val required: Boolean get() = true
    private val setter: (D, V?) -> Unit
    private val getter: (D) -> V

    constructor(
        name: String,
        domainClass: KClass<D>,
        valueClass: KClass<V>,
        setter: (D, V?) -> Unit,
        getter: (D) -> V
    ) : super(name, domainClass, valueClass) {
        this.setter = setter
        this.getter = getter
    }

    override fun of(domain: D): V = getter(domain)
    override fun set(domain: D, value: V?) = setter(domain, value)
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

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>> {
    val left : Criterion<D, Operator, out Any?>
    val right : Criterion<D, Operator, out Any?>
    override val operator: BinaryOperator
    override val domainClass: KClass<D> get() = left.domainClass

    constructor(
        left: Criterion<D, out Operator, out Any?>,
        operator: BinaryOperator,
        right: Criterion<D, out Operator, out Any?>
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
        return when (operator) {
            BinaryOperator.NOT -> /**/ "$operator (${right.invoke()})"
            else -> "(${left.invoke()}) $operator (${right.invoke()})"
        }
    }

    /** Extenced text expression */
    override fun toString(): String {
        return "${domainClass.simpleName}: ${invoke()}"
    }
}

open class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val property : PropertyNullable<D, out V>
    val value : V?
    override val operator: ValueOperator
    override val domainClass: KClass<D> get() = property.domainClass

    constructor(property: PropertyNullable<D, out V>, operator: ValueOperator, value: V) {
        this.property = property
        this.operator = operator
        this.value = value
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            ValueOperator.ALL -> true
            ValueOperator.NONE -> false
            ValueOperator.EQ -> property.of(domain) == value
            ValueOperator.GT ->  compare(property.of(domain), value) > 0
            ValueOperator.GTE -> compare(property.of(domain), value) >= 0
            ValueOperator.LT -> compare(property.of(domain), value) < 0
            ValueOperator.LTE -> compare(property.of(domain), value) <= 0
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
            throw IllegalStateException("Unsupported comparation for ${this.property.valueClass}" )
        }
    }

    override operator fun invoke(): String {
        val separator = stringValueSeparator()
        return "$property $operator $separator$value$separator"
    }

    override fun toString(): String {
        return "${property.domainClass.simpleName}: ${invoke()}"
    }

    /** A separator for String values */
    private fun stringValueSeparator() : String {
        return if (value is CharSequence) "\"" else ""
    }
}

/** Interface of the domain meta-model */
interface AbstractModelProvider {
    /** Get all entity models */
    val entityModels : List<DomainModel>
}

/** Meta-model of the domain object will be a generated class in the feature */
interface DomainModel {
    /** Get the main domain class */
    val _domainClass : KClass<*>
    /** Get all properties */
    val _properties : List<PropertyNullable<out Any, Any>>
}
