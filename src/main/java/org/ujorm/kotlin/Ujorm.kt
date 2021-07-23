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

import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.reflect
import kotlin.streams.toList

interface Operator

interface Criterion<D : Any, out OP : Operator, out V : Any?> {
    val entityClass : KClass<D>
    val operator: OP
    fun eval(entity : D) : Boolean
    fun not() : Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>>
            = BinaryCriterion(this, BinaryOperator.NOT, this)
    /** Plain text expression */
    operator fun invoke(): String
    /** Extended text expression */
    override fun toString(): String

    infix fun AND(crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND, crn)
    }
    infix fun OR (crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR, crn)
    }
    infix fun AND_NOT (crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND_NOT, crn)
    }
    infix fun OR_NOT (crn: Criterion<D, Operator, Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR_NOT, crn)
    }
}

/** Property descriptor for nullable values */
interface PropertyNullable<D : Any, V : Any> : CharSequence {
    val index : Short
    val name : String
    /** Is the value required (non-null) ? */
    val required : Boolean
    val entityClass : KClass<D>
    val valueClass : KClass<out V>
    val readOnly : Boolean

    /** Get a value from the entity */
    fun of(entity : D) : V?

    /** Set a value to the entity */
    fun set(entity: D, value: V?) : Unit

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
    /** Get a value from the entity */
    override fun of(entity : D) : V

    /** Set a non-null value to the entity */
    override fun set(entity: D, value: V?) : Unit
}

/** Abstract property descriptor */
abstract class AbstractProperty<D : Any, V : Any> : PropertyNullable<D, V> {
    override val index: Short
    override var name: String
        internal set(value) { field = if ((field?:"").isEmpty()) value else throw IllegalStateException("Name is: $field") }
        public get() = field
    /** Required value (mon-nnull)
     * KType = typeOf<Int?>()  */
    override val entityClass: KClass<D>
    override val valueClass: KClass<V>
    override val readOnly: Boolean

    constructor(index : Short, name: String, entityClass: KClass<D>, valueClass: KClass<V>,) {
        this.index = index
        this.name = name
        this.entityClass = entityClass
        this.valueClass = valueClass
        this.readOnly = false // TODO: set the value according to property type
    }

    /** For a CharSequence implementation */
    override val length: Int get() = name.length

    /** For a CharSequence implementation */
    override fun get(index: Int): Char = name[index]

    /** For a CharSequence implementation */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)

    /** For a CharSequence implementation */
    override fun toString(): String = name

    /** Equals */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AbstractProperty<*, *>
        if (entityClass != other.entityClass) return false
        if (name != other.name) return false

        return true
    }

    /** HashCode */
    override fun hashCode(): Int {
        return entityClass.hashCode() * 31 + name.hashCode()
    }
}

/** Property descriptor for nullable values */
open class PropertyNullableImpl<D : Any, V : Any> : AbstractProperty<D, V> {
    override val required: Boolean get() = false
    private val setter: (D, V?) -> Unit
    private val getter: (D) -> V?

    constructor(
        index: Short,
        name: String,
        getter: (D) -> V?,
        setter: (D, V?) -> Unit,
        entityClass: KClass<D>,
        valueClass: KClass<V> = getter.reflect()!!.returnType!!.classifier as KClass<V>,
    ) : super(index, name, entityClass, valueClass) {
        this.getter = getter
        this.setter = setter
    }

    override fun of(entity: D): V? = getter(entity)
    override fun set(entity: D, value: V?) = setter(entity, value)
}

/** Property for non-null values */
open class PropertyImpl<D : Any, V : Any> : AbstractProperty<D, V> , Property<D, V> {
    override val required: Boolean get() = true
    private val getter: (D) -> V
    private val setter: (D, V?) -> Unit

    /** Original constructor */
    constructor(
        index: Short,
        name: String,
        getter: (D) -> V,
        setter: (D, V?) -> Unit,
        entityClass: KClass<D>,
        valueClass: KClass<V> = getter.reflect()!!.returnType!!.classifier as KClass<V>,
    ) : super(index, name, entityClass, valueClass) {
        this.getter = getter
        this.setter = setter
    }

    override fun of(entity: D): V = getter(entity)
    override fun set(entity: D, value: V?) = setter(entity, value)
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
    override val entityClass: KClass<D> get() = left.entityClass

    constructor(
        left: Criterion<D, out Operator, out Any?>,
        operator: BinaryOperator,
        right: Criterion<D, out Operator, out Any?>
    ) {
        this.left = left
        this.operator = operator
        this.right = right
    }

    override fun eval(entity: D): Boolean {
        return when(operator) {
            BinaryOperator.AND -> left.eval(entity) && right.eval(entity)
            BinaryOperator.OR -> left.eval(entity) || right.eval(entity)
            BinaryOperator.AND_NOT -> left.eval(entity) && !right.eval(entity)
            BinaryOperator.OR_NOT -> left.eval(entity) || !right.eval(entity)
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
        return "${entityClass.simpleName}: ${invoke()}"
    }
}

open class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val property : PropertyNullable<D, out V>
    val value : V?
    override val operator: ValueOperator
    override val entityClass: KClass<D> get() = property.entityClass

    constructor(property: PropertyNullable<D, out V>, operator: ValueOperator, value: V) {
        this.property = property
        this.operator = operator
        this.value = value
    }

    override fun eval(entity: D): Boolean {
        return when(operator) {
            ValueOperator.ALL -> true
            ValueOperator.NONE -> false
            ValueOperator.EQ -> property.of(entity) == value
            ValueOperator.GT ->  compare(property.of(entity), value) > 0
            ValueOperator.GTE -> compare(property.of(entity), value) >= 0
            ValueOperator.LT -> compare(property.of(entity), value) < 0
            ValueOperator.LTE -> compare(property.of(entity), value) <= 0
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
        return "${property.entityClass.simpleName}: ${invoke()}"
    }

    /** A separator for String values */
    private fun stringValueSeparator() : String {
        return if (value is CharSequence) "\"" else ""
    }
}

/** Interface of the domain meta-model */
abstract class AbstractModelProvider {
    /** Get all entity models */
    val entityModels: List<EntityModel<*>> by lazy {
        val result : List<EntityModel<*>> = Utils.getProperties(this, EntityModel::class)
        result.sortedBy { it._entityClass.simpleName }
    }
}

/** Model of the entity will be generated in the feature */
abstract class EntityModel<D : Any> (
    /** Get the main domain class */
    val _entityClass : KClass<D>,
    private var _size : Short = 0
) {
    /** Get all properties */
    val _properties: List<PropertyNullable<D, Any>> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val result : List<PropertyNullable<D, Any>> = Utils.getProperties(this, PropertyNullable::class)
        result.sortedBy { it.index }
    }

    fun init() : EntityModel<D> {
        val map = Utils.getPropertyNames(this)
        for (p in _properties) {
            val name = map.getOrDefault(p.index, "")
            if (!name.isEmpty() && p is AbstractProperty) p.name = name
        }
        return this;
    }

    /** Create a non-null property */
    protected fun <V : Any> property(
        name : String = "",
        getter : (D) -> V,
        setter : (D, V?) -> Unit
    ) : Property<D, V> = PropertyImpl<D, V> (_size++, name, getter, setter, _entityClass);

    /** Create a non-null property */
    protected fun <V : Any> property(
        getter : (D) -> V,
        setter : (D, V?) -> Unit
    ) : Property<D, V> = PropertyImpl<D, V> (_size++, "", getter, setter, _entityClass);

    /** Create a nullable property */
    protected fun <V : Any> propertyN6e(
        name : String,
        getter : (D) -> V?,
        setter : (D, V?) -> Unit
    ) : PropertyNullable<D, V> = PropertyNullableImpl<D, V> (_size++, name, getter, setter, _entityClass);

    /** Create a nullable property */
    protected fun <V : Any> propertyN6e(
        getter : (D) -> V?,
        setter : (D, V?) -> Unit
    ) : PropertyNullable<D, V> = PropertyNullableImpl<D, V> (_size++, "", getter, setter, _entityClass);
}

/** Common utilities */
internal object Utils {
    /** Get all properties of the instance for a required types */
    fun <V : Any> getKProperties(instance: Any, type: KClass<in V> ) : Stream<KProperty1<Any, V>> =
        instance::class.members.stream()
            .filter { property -> property is KProperty1<*, *> }
            .map { property -> property as KProperty1<Any, V> }
            .filter { property -> isPropertyTypeOf(property, type) }

    /** Get all properties of the instance for a required types */
    fun <V : Any> getProperties(instance: Any, type: KClass<in V> ) : List<V> =
        getKProperties(instance, type)
            .map { property -> property.getter.call(instance) as V}
            .toList()

    /** Check if the property value has required type */
    fun <V : Any> isPropertyTypeOf(property: KProperty1<Any, *>, targetClass: KClass<V>): Boolean {
        val classifier: KClassifier? = property.getter.returnType.classifier;
        val properClass: KClass<*> = if (classifier is KClass<*>) classifier else Unit::class
        return targetClass.isSuperclassOf(properClass)
    }

    /** Get a maps: index to name */
    fun getPropertyNames(instance: Any): Map<Short, String> {
        return getKProperties(instance, PropertyNullable::class)
            .toList()
            .map { it.getter.call(instance).index to it.name }
            .toMap();
    }


    /** Get a maps: index to name */
    fun getPropertyNamesOrig(instance: Any): Map<Short, String> {
        return getKProperties(instance, PropertyNullable::class)
            .toList()
            .map { it.getter.call(instance).index to it.name }
            .toMap();
    }
}
