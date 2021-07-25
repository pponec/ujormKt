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

import java.lang.IllegalArgumentException
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.reflect
import kotlin.streams.toList

interface Operator {
    /** An operator name */
    val name : String
}

interface ValueOperator : Operator {
    /** Evaluate condition */
    fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?): Boolean
}

interface Criterion<D : Any, out OP : Operator, out V : Any?> {
    val entityClass: KClass<D>
    val operator: OP
    fun evaluate(entity: D): Boolean
    fun not(): Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>> =
        BinaryCriterion(this, BinaryOperator.NOT, this)

    /** Plain text expression */
    operator fun invoke(): String

    /** An shortcut for an evaluation */
    operator fun invoke(entity: D) = evaluate(entity)

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

/** A root of properties */
interface CommonProperty<D : Any, V : Any> : CharSequence {
    val index: Short
    val name: String

    /** Is the value required (non-null) ? */
    val required: Boolean
    val entityClass: KClass<D>
    val valueClass: KClass<out V>
    val readOnly: Boolean

    /** A shortcut for the of() method */
    operator fun invoke(entity: D): V? = of(entity)

    /** A shortcut for the set() method */
    operator fun invoke(entity: D, value: V?): Unit = set(entity, value)

    /** Get a value from the entity */
    fun of(entity: D): V?

    /** Set a value to the entity */
    fun set(entity: D, value: V?): Unit

    fun operate(operator: ValueOperator, value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    /** Returns a property name introduced by a simple domain name */
    fun info(): String = "${entityClass.simpleName}.$name"

    /** Name of property */
    operator fun invoke(): String = info()

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
    infix fun LT(value: V): ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperatorEnum.LT, value)
    }
}

/** A property descriptor for nullable values */
interface NullableProperty<D : Any, V : Any> : CommonProperty<D, V>

/** A property descriptor for mandatory values */
interface MandatoryProperty<D : Any, V : Any> : CommonProperty<D, V> {

    /** A shortcut for the of() method */
    override operator fun invoke(entity: D): V = of(entity)

    /** Get a value from the entity */
    override fun of(entity: D): V

    /** Set a non-null value to the entity */
    override fun set(entity: D, value: V?): Unit
}

/** Abstract property descriptor */
abstract class AbstractProperty<D : Any, V : Any> : CommonProperty<D, V> {
    override val index: Short
    override var name: String
        internal set(value) {
            // Note: field.isEmpty() expression throws the NullPointerException in Kotlin 1.5.21
            field = if (field?.isEmpty() ?: true) value else throw IllegalStateException("Name is: $field")
        }

    /** Required value (mon-nnull)
     * KType = typeOf<Int?>()  */
    override val entityClass: KClass<D>
    override val valueClass: KClass<V>
    override val readOnly: Boolean

    constructor(index: Short, name: String, entityClass: KClass<D>, valueClass: KClass<V>) {
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
open class NullablePropertyImpl<D : Any, V : Any> : AbstractProperty<D, V>, NullableProperty<D, V> {
    override val required: Boolean get() = false
    private val getter: (D) -> V?
    internal var setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
        internal set(value) {
            field = if (field == Constants.UNDEFINED_SETTER) value
            else throw IllegalStateException("${entityClass.simpleName}.$index")
        }

    constructor(
        index: Short,
        name: String,
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        entityClass: KClass<D>,
        valueClass: KClass<V> = getter.reflect()!!.returnType.classifier as KClass<V>,
    ) : super(index, name, entityClass, valueClass) {
        this.getter = getter
        this.setter = setter
    }

    override fun of(entity: D): V? = getter(entity)
    override fun set(entity: D, value: V?) = setter(entity, value)
}

/** Property for mandatory values */
open class MandatoryPropertyImpl<D : Any, V : Any> : AbstractProperty<D, V>, MandatoryProperty<D, V> {
    override val required: Boolean get() = true
    private val getter: (D) -> V
    internal var setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
        internal set(value) {
            field = if (field == Constants.UNDEFINED_SETTER) value
            else throw IllegalStateException("${entityClass.simpleName}.$index")
        }

    /** Original constructor */
    constructor(
        index: Short,
        name: String,
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        entityClass: KClass<D>,
        valueClass: KClass<V> = getter.reflect()!!.returnType.classifier as KClass<V>,
    ) : super(index, name, entityClass, valueClass) {
        this.getter = getter
        this.setter = setter
    }

    override fun of(entity: D): V = getter(entity)
    override fun set(entity: D, value: V?) = setter(entity, value
        ?: throw IllegalArgumentException("Mandatory property: ${info()}"))
}

/** An operator for a BinaryCriterion */
enum class BinaryOperator : Operator {
    AND,
    OR,
    NOT,
    AND_NOT,
    OR_NOT;
}

/** An operator for a ValueCriterion */
enum class ValueOperatorEnum : ValueOperator {
    EQ {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) =
            property.of(entity) == value
    },
    LT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) =
            compare(entity, property, value) < 0
    },
    GT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) =
            compare(entity, property, value) > 0
    },
    GTE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) =
            compare(entity, property, value) >= 0
    },
    ALL {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) = true
    },
    NONE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: CommonProperty<D, out V>, value: V?) = false
    };

    /** Comparator */
    protected fun <D : Any, V : Any> compare(entity: D, property: CommonProperty<D, out V>, value: V?) =
        compareValues(property.of(entity), value, property)

    /** Comparator */
    protected fun <D : Any, V : Any> compareValues(a: V?, b: V?, property: CommonProperty<D, out V>): Int {
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

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any?>> {
    val left: Criterion<D, Operator, out Any?>
    val right: Criterion<D, Operator, out Any?>
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

    override fun evaluate(entity: D): Boolean {
        return when (operator) {
            BinaryOperator.AND -> left(entity) && right(entity)
            BinaryOperator.OR -> left(entity) || right(entity)
            BinaryOperator.NOT -> !left(entity)
            BinaryOperator.AND_NOT -> left(entity) && !right(entity)
            BinaryOperator.OR_NOT -> left(entity) || !right(entity)
            else -> {
                throw UnsupportedOperationException("Unsupported operator: ${operator.name}")
            }
        }
    }

    /** Plain text expression */
    override operator fun invoke(): String {
        return when (operator) {
            BinaryOperator.NOT -> /**/ "${operator.name} (${right.invoke()})"
            else -> "(${left.invoke()}) ${operator.name} (${right.invoke()})"
        }
    }

    /** Extended text expression */
    override fun toString(): String {
        return "${entityClass.simpleName}: ${invoke()}"
    }
}

open class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val property: CommonProperty<D, out V>
    val value: V?
    override val operator: ValueOperator
    override val entityClass: KClass<D> get() = property.entityClass

    constructor(property: CommonProperty<D, out V>, operator: ValueOperator, value: V?) {
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
            //@Suppress("UNCHECKED_CAST")
            (a as Comparable<T>).compareTo(b)
        } else {
            throw IllegalStateException("Unsupported comparation for ${property.info()}")
        }
    }

    override operator fun invoke(): String {
        val separator = stringValueSeparator()
        return "$property ${operator.name} $separator$value$separator"
    }

    override fun toString(): String {
        return "${property.entityClass.simpleName}: ${invoke()}"
    }

    /** A separator for String values */
    private fun stringValueSeparator(): String {
        return if (value is CharSequence) "\"" else ""
    }
}

/** Interface of the domain meta-model */
abstract class AbstractModelProvider {
    protected val SYNCHRONIZED = LazyThreadSafetyMode.SYNCHRONIZED

    /** Get all entity models */
    val entityModels: List<EntityModel<*>> by lazy {
        val result: List<EntityModel<*>> = Utils.getProperties(this, EntityModel::class)
        result.sortedBy { it._entityClass.simpleName }
    }
}

/** Model of the entity will be generated in the feature */
abstract class EntityModel<D : Any>(
    /** Get the main domain class */
    val _entityClass: KClass<D>,
    private var _size: Short = 0
) {
    /** Get all properties */
    val _properties: List<CommonProperty<D, Any>> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val result: List<CommonProperty<D, Any>> = Utils.getProperties(this, CommonProperty::class)
        result.sortedBy { it.index }
    }

    /** Initialize all properties */
    fun init(): EntityModel<D> {
        val map = Utils.getKPropertyMap(this)
        for (p in _properties) {
            val kProperty = map.get(p.index)
                ?: throw IllegalStateException("Property not found: ${p.entityClass.simpleName}.${p.index}")
            Utils.assignName(p, kProperty)
            Utils.assignSetter(p)
        }
        return this
    }

    /** Create an Entity builder */
    fun builder(): EntityBuilder<D> = EntityBuilder(this)

    /** Create a non-null property */
    protected fun <V : Any> property(
        name: String = "",
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ): MandatoryProperty<D, V> = MandatoryPropertyImpl<D, V>(_size++, name, getter, setter, _entityClass)

    /** Create a non-null property */
    protected fun <V : Any> property(
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ): MandatoryProperty<D, V> = MandatoryPropertyImpl<D, V>(_size++, "", getter, setter, _entityClass)

    /** Create a nullable property */
    protected fun <V : Any> propertyN6e(
        name: String,
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ): NullableProperty<D, V> = NullablePropertyImpl<D, V>(_size++, name, getter, setter, _entityClass)

    /** Create a nullable property */
    protected fun <V : Any> propertyN6e(
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ): NullableProperty<D, V> = NullablePropertyImpl<D, V>(_size++, "", getter, setter, _entityClass)

    override fun toString() = _entityClass.simpleName ?: "?"
}

/** Common utilities */
internal object Utils {
    /** Get all properties of the instance for a required types */
    fun <V : Any> getKProperties(instance: Any, type: KClass<in V>): Stream<KProperty1<Any, V>> =
        instance::class.members.stream()
            .filter { it is KProperty1<*, *> }
            .map { it as KProperty1<Any, V> }
            .filter { isPropertyTypeOf(it, type) }

    /** Get all properties of the instance for a required types */
    fun <V : Any> getProperties(instance: Any, type: KClass<in V>): List<V> =
        getKProperties(instance, type)
            .map { it(instance) }
            .toList()

    /** Check if the property value has required type */
    fun <V : Any> isPropertyTypeOf(property: KProperty1<Any, *>, targetClass: KClass<V>): Boolean {
        val classifier: KClassifier? = property.getter.returnType.classifier
        val properClass: KClass<*> = if (classifier is KClass<*>) classifier else Unit::class
        return targetClass.isSuperclassOf(properClass)
    }

    /** Get a maps: index to KProperty1 */
    fun <D : Any> getKPropertyMap(instance: D): Map<Short, KProperty1<D, CommonProperty<D, *>>> =
        getKProperties(instance, CommonProperty::class)
            .toList()
            .map { it(instance).index to it as KProperty1<D, CommonProperty<D, *>> }
            .toMap()

    /** Assign a property name to the uProperty */
    fun <D : Any> assignName(
        uProperty: CommonProperty<D, Any>,
        kProperty: KProperty1<EntityModel<D>, CommonProperty<EntityModel<D>, *>>
    ) {
        val name = kProperty.name
        if (uProperty.name.isEmpty()
            && !name.isEmpty()
            && uProperty is AbstractProperty
        ) {
            uProperty.name = name
        }
    }

    /** Assign a property setter to the uProperty */
    fun <D : Any> assignSetter(uProperty: CommonProperty<D, Any>) {
        val eProperty = uProperty.entityClass.memberProperties.find { it.name == uProperty.name }
        if (eProperty is KMutableProperty<*>) when (uProperty) {
            is NullablePropertyImpl -> {
                if (uProperty.setter === Constants.UNDEFINED_SETTER)
                    uProperty.setter = { d, v -> eProperty.setter.call(d, v) }
            }
            is MandatoryPropertyImpl -> {
                if (uProperty.setter === Constants.UNDEFINED_SETTER)
                    uProperty.setter = { d, v -> eProperty.setter.call(d, v) }
            }
        }
    }
}

/** Entity builder */
open class EntityBuilder<D : Any>(
    val model: EntityModel<D>,
) {
    private val map = mutableMapOf<String, Any?>()

    /** Set a value to an internal store */
    fun <V: Any> set(property: NullableProperty<D, V>, value: Any?) {
        map[property.name] = value
    }

    /** Set a value to an internal store */
    fun <V: Any> set(property: MandatoryProperty<D, V>, value: Any) {
        map[property.name] = value
    }

    /** Build a entity object */
    fun build(): D {
        val constructor = model._entityClass.constructors
            .stream()
            .filter{ c -> c.parameters.size == map.size}
            .findFirst()
            .orElseThrow {IllegalStateException("No constructor[${map.size}] found")}
        val params = constructor.parameters
            .stream()
            .map { kParam -> map[kParam.name] }
            .toArray();
        return constructor.call(*params)
    }

    override fun toString() = model._entityClass.simpleName ?: "?"
}

/** @see https://stackoverflow.com/questions/44038721/constants-in-kotlin-whats-a-recommended-way-to-create-them */
object Constants {
    /** Undefined property setter */
    val UNDEFINED_SETTER: (d: Any, v: Any?) -> Unit = { d, v -> throw UnsupportedOperationException("read-only") }

}