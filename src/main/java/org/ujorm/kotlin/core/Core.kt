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

import org.ujorm.kotlin.core.impl.*
import org.ujorm.kotlin.orm.AbstractDatabase
import java.util.*
import kotlin.reflect.KClass

/** Common condition operator */
interface Operator {
    /** An operator name */
    val name: String
}

interface ValueOperator : Operator {
    /** Evaluate condition */
    fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?): Boolean
}

/** API of the object condition */
interface Criterion<D : Any, out OP : Operator, out V : Any?> {
    val entityClass: KClass<D>
    val operator: OP
    fun evaluate(entity: D): Boolean
    fun not(): Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>> =
        BinaryCriterion(this, BinaryOperator.NOT, this)

    /** Plain text expression */
    operator fun invoke(): String

    /** A shortcut for an evaluation */
    operator fun invoke(entity: D) : Boolean = evaluate(entity)

    /** Extended text expression */
    override fun toString(): String

    infix fun AND(crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND, crn)
    }

    infix fun OR(crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR, crn)
    }

    infix fun AND_NOT(crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND_NOT, crn)
    }

    infix fun OR_NOT(crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR_NOT, crn)
    }
}

interface CriterionProvider<D: Any, DB: AbstractDatabase> {
    /**
     * The condition of relationship 1:M limits the number of foreign rows.
     * The entity type of D corresponds to the type on which we want to limit the number of rows.
     */
    fun get(database: DB): Criterion<D, *, *>
}

/** API of the property metadata descriptor */
interface PropertyMetadata<D : Any, V : Any> {
    /** Index of the property */
    val index: UByte
    val name: String
    val entityClass: KClass<D>
    val valueClass: KClass<V> // KClass<out V>
    /** Is the property value read-only? */
    val readOnly: Boolean
    /** Variables of this property can have null value. */
    val nullable: Boolean
    /** Variables of this property must be non-null. */
    val required get() = !nullable
    /** Entity type */
    val entityType : ClassType
    /** Number of direct properties. A base property has a value of one. */
    val level : UByte
    /** Is the property composed? */
    fun isComposed() = index == UByte.MAX_VALUE
    fun indexToInt() = index.toInt()

    /** State of properties */
    fun status(): String
}

/** API of the list property metadata descriptor */
interface ListPropertyMetadata<D : Any, I: Any> : PropertyMetadata<D, List<I>> {
    val itemClass: KClass<I>
}

/** API of the direct property descriptor for a nullable values. */
interface PropertyNullable<D : Any, V : Any> : CharSequence {
    fun data() : PropertyMetadata<D, V>

    /** An entity alias name where a blank text means the default value. */
    fun entityAlias() : String

    /** Clone this property with a new alias */
    fun entityAlias(entityAlias: String) : PropertyNullable<D, V>

    /** Clone this property with a new alias */
    operator fun invoke(entityAlias: String): PropertyNullable<D, V> = entityAlias(entityAlias)

    /** Ascending sort request */
    fun ascx() : SortingProperty<D, V> = ASCENDING(true)
    /** Descending sort request */
    fun descx() : SortingProperty<D, V> = ASCENDING(false)
    /** Sorting direction determined by a parameter. */
    infix fun ASCENDING(ascending: Boolean) = SortingProperty(this, ascending)

    /** Get a value from the entity */
    operator fun get(entity: D): V?

    /** Set a value to the entity */
    operator fun set(entity: D, value: V?)

    @Suppress("UNCHECKED_CAST")
    operator fun get(entity: Array<Any?>): V? =
        entity[data().indexToInt()] as V?

    /** Set a value to the entity */
    operator fun set(entity: Array<Any?>, value: V?) {
        require(value != null || data().nullable) { "${info()} is required" }
        entity[data().indexToInt()] = value }

    fun operate(operator: ValueOperator, value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    /** Returns a property name introduced by a simple domain name.
     * For not a valid alias is result by the template: {@sample e(Employee).id } */
    fun info(): String {
        val data = data()
        val entityAlias = entityAlias()
        val entityClassName = data.entityClass.simpleName
        return if (entityAlias.isEmpty())
            "$entityClassName.${data.name}"
        else "$entityClassName($entityAlias).${data.name}"
    }

    /** Returns a property name introduced by a simple domain name.
     * For not a valid alias is result by the template: {@sample e(Employee).id } */
    fun status(): String =
        "${info()} {${data().status()}}"

    /** Returns a simple name of the property */
    fun name(): String = data().name

    /** Returns a simple name of the property */
    operator fun invoke(): String = name()

    /** Criterion evaluates the true value always */
    fun forAll(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.ALL, null)
    }

    /** Criterion evaluates the false value always */
    fun forNone(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.NONE, null)
    }

    /** Value operator */
    infix fun EQ(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.EQ, value)
    }

    /** Value operator */
    infix fun GT(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.GT, value)
    }

    /** Value operator */
    infix fun GE(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.GE, value)
    }

    /** Value operator */
    infix fun LT(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.LT, value)
    }

    /** Value operator */
    infix fun LE(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.LE, value)
    }

    /** Value operator */
    infix fun STARTS(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.STARTS, value)
    }

    /** Create new composite property  */
    operator fun <N: Any> plus(nextProperty : PropertyNullable<V, N>) : PropertyNullable<D, N> =
        ComposedPropertyNullableImpl(this, nextProperty)


    // --- CharSequence implementation based on info() method ---

    /** For a CharSequence implementation */
    override val length: Int get() = info().length

    /** For a CharSequence implementation */
    override fun get(index: Int): Char = info()[index]

    /** For a CharSequence implementation */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = info().subSequence(startIndex, endIndex)
}

/** API of the direct property descriptor */
interface Property<D : Any, V : Any> : PropertyNullable<D, V> {
    override operator fun get(entity: D): V

    /** Get a value from the entity */
    @Suppress("UNCHECKED_CAST")
    override operator fun get(entity: Array<Any?>): V =
        entity[data().indexToInt()] as V

    /** Create new composite property */
    operator fun <N: Any> plus(nextProperty : Property<V, N>) : Property<D, N> =
        ComposedPropertyImpl(this, nextProperty)

    /** Clone this property with a new alias */
    override fun entityAlias(entityAlias : String) : Property<D, V>

    /** Clone this property with a new alias */
    override operator fun invoke(entityAlias : String): Property<D, V> = entityAlias(entityAlias)
}

/** API of the direct list property descriptor */
interface ListProperty<D : Any, V : Any> : Property<D, List<V>> {

    override fun data(): ListPropertyMetadata<D, V>
}

/** An operator for a BinaryCriterion */
enum class BinaryOperator : Operator {
    AND,
    OR,
    NOT,
    AND_NOT,
    OR_NOT;
}

/** Class type for building new objects. */
enum class ClassType {
    INTERFACE,
    DATACLASS,
    ABSTRACT;

    companion object {
        fun of(clazz : KClass<*>) : ClassType {
            return when {
                clazz.java.isInterface -> INTERFACE
                clazz.isAbstract -> ABSTRACT
                else -> DATACLASS
            }
        }
    }
}


/** An operator for a ValueCriterion */
enum class ValueOperatorEnum : ValueOperator {
    /** Equals to */
    EQ {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            property[entity] == value
    },
    /** Less than */
    LT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            isEqual(entity, property, value) < 0
    },
    /** Less than or equals to */
    LE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            isEqual(entity, property, value) < 0
    },
    /** Great than */
    GT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            isEqual(entity, property, value) > 0
    },
    /** Great than or equals to */
    GE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            isEqual(entity, property, value) >= 0
    },
    /** Text starts by the value */
    STARTS {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) : Boolean {
            val propertyValue = property[entity]
            if (propertyValue == value) return true
            if (propertyValue == null || value == null) return false
            if (propertyValue is String && value is String) {
                return propertyValue.startsWith(value)
            } else {
                return false
            }
        }
    },
    /** For all items */
    ALL {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) = true
    },
    /** For no item */
    NONE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) = false
    };

    /** Comparator */
    protected fun <D : Any, V : Any> isEqual(entity: D, property: PropertyNullable<D, out V>, value: V?) =
        compareValues(property[entity], value, property)

    /** Comparator */
    protected fun <D : Any, V : Any> compareValues(a: V?, b: V?, property: PropertyNullable<D, out V>): Int {
        if (a === b) return 0
        if (a == null) return -1
        if (b == null) return 1

        return if (a is Comparable<*>) {
            @Suppress("UNCHECKED_CAST")
            (a as Comparable<V>).compareTo(b)
        } else {
            throw IllegalStateException("Unsupported comparation for ${property.info()}")
        }
    }
}
