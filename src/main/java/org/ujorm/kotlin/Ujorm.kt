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
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties

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

/** API of the property descriptor for a nullable values */
interface PropertyNullable<D : Any, V : Any> : CharSequence {
    /** Index of the property */
    val index: UByte
    val name: String
    val entityClass: KClass<D>
    val valueClass: KClass<out V> //  KClass<out V>
    /** Is the property value read-only? */
    val readOnly: Boolean
    /** Is the value nullable? */
    val nullable: Boolean

    /** Get a value from the entity */
    operator fun get(entity: D): V?
    /** Set a value to the entity */
    operator fun set(entity: D, value: V?)

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

    // --- CharSequence implementation ---

    /** For a CharSequence implementation */
    override val length: Int get() = name.length

    /** For a CharSequence implementation */
    override fun get(index: Int): Char = name[index]

    /** For a CharSequence implementation */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)
}

/** API of the property descriptor */
interface Property<D : Any, V : Any> : PropertyNullable<D, V> {
    override operator fun get(entity: D): V
}

/** An implementation of the property descriptor for nullable values */
open class PropertyNullableImpl<D : Any, V : Any> : PropertyNullable<D, V>, CharSequence {
    final override val index: UByte
    override var name: String
        internal set(value) {
            // Note: field.isEmpty() expression throws the NullPointerException in Kotlin 1.5.21
            field = if (field?.isEmpty() ?: true) value else throw IllegalStateException("Name was assigned to: $field")
        }
    final override val entityClass: KClass<D>
    final override val valueClass: KClass<V> //  KClass<out V>
    final override val readOnly: Boolean

    /** Is the value nullable or required ? */
    final override val nullable: Boolean
    override fun get(entity: D): V? = getter.invoke(entity)
    override fun set(entity: D, value: V?) = setter.invoke(entity, value)
    /** Value provider is not the part of API */
    open internal val getter: (D) -> V?
    /** Value writer is not the part of API */
    open internal var setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
        set(value) {
            field = if (field == Constants.UNDEFINED_SETTER) value
            else throw IllegalStateException("${entityClass.simpleName}.$index")
        }

    constructor(
        index: UByte,
        name: String = "",
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        entityClass: KClass<D>,
        valueClass: KClass<V>, // KClass<out V>
        readOnly: Boolean = false,
        nullable: Boolean = false,
        ) {
        this.index = index
        this.name = name
        this.entityClass = entityClass
        this.valueClass = valueClass
        this.readOnly = readOnly
        this.nullable = nullable
        this.getter = getter
        this.setter = setter
    }

    override fun toString(): String {
        try {
            return name
        } catch (ex: IllegalStateException) {
            return "null"
        }
    }

    /** Equals */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PropertyNullable<*, *>
        if (entityClass != other.entityClass) return false
        if (name != other.name) return false

        return true
    }

    /** HashCode */
    override fun hashCode(): Int {
        return entityClass.hashCode() * 31 + name.hashCode()
    }
}

/** An implementation of the property descriptor */
class PropertyImpl<D : Any, V : Any>(
    index: UByte,
    name: String,
    getter: (D) -> V,
    setter: (D, V?) -> Unit,
    entityClass: KClass<D>,
    valueClass: KClass<V>,
    readOnly: Boolean,
    nullable: Boolean
) : Property<D, V>,
    PropertyNullableImpl<D, V>(index, name, getter, setter, entityClass, valueClass, readOnly, nullable) {

    override val getter: (D) -> V = super.getter as (D) -> V
    override fun set(entity: D, value: V?) = setter.invoke(entity, value
        ?: throw IllegalArgumentException("Notnull value is required"))

    override fun get(entity: D): V = getter.invoke(entity)
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
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            property[entity] == value
    },
    LT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            compare(entity, property, value) < 0
    },
    GT {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            compare(entity, property, value) > 0
    },
    GTE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) =
            compare(entity, property, value) >= 0
    },
    ALL {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) = true
    },
    NONE {
        override fun <D : Any, V : Any> evaluate(entity: D, property: PropertyNullable<D, out V>, value: V?) = false
    };

    /** Comparator */
    protected fun <D : Any, V : Any> compare(entity: D, property: PropertyNullable<D, out V>, value: V?) =
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
                throw UnsupportedOperationException("Unsupported operator: ${operator.name}")
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
    override val entityClass: KClass<D> get() = property.entityClass

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
    private var _size: UByte = 0U
) {
    /** Get all properties */
    val _properties: List<PropertyNullable<D, Any>> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val result: List<PropertyNullable<D, Any>> = Utils.getProperties(this, PropertyNullable::class)
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

    /** Create a non-null property.
     * NOTE: The property field must heave the same as the original Entity, or use the same name by a name argument.
     **/
    inline protected fun <reified V : Any> property(
        name: String,
        noinline getter: (D) -> V,
        noinline setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ) = propertyInternal(getter, setter, V::class, name)

    /** Create a non-null property.
     * NOTE: The property field must heave the same as the original Entity, or use the same name by a name argument.
     **/
    inline protected fun <reified V : Any> property(
        noinline getter: (D) -> V,
        noinline setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ) = propertyInternal(getter, setter, V::class)


    /** Create a nonnull property.
     * NOTE: The property field must heave the same as the original Entity, or use the same name by a name argument.
     **/
    protected fun <V : Any> propertyInternal(
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        valueClass: KClass<V>,
        name: String = ""
    ): Property<D, V> = PropertyImpl<D, V>(
        _size++, "", getter, setter, _entityClass, valueClass, false, false)

    /** Create a non-null property.
     * NOTE: The property field must heave the same as the original Entity, or use the same name by a name argument.
     **/
    inline protected fun <reified V : Any> propertyNullable(
        noinline getter: (D) -> V?,
        noinline setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ) = propertyNullableInternal(getter, setter, V::class)


    /** Create a nonnull property.
     * NOTE: The property field must heave the same as the original Entity, or use the same name by a name argument.
     **/
    protected fun <V : Any> propertyNullableInternal(
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        valueClass: KClass<V>,
        name: String = ""

    ) = PropertyNullableImpl<D, V>(
        _size++, "", getter, setter, _entityClass, valueClass, false, true)
    override fun toString() = _entityClass.simpleName ?: "null"
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
    fun <D : Any> getKPropertyMap(instance: D): Map<UByte, KProperty1<D, PropertyNullable<D, *>>> =
        getKProperties(instance, PropertyNullable::class)
            .toList()
            .map { it(instance).index to it as KProperty1<D, PropertyNullable<D, *>> }
            .toMap()

    /** Assign a property name to the uProperty */
    fun <D : Any> assignName(
        uProperty: PropertyNullable<D, Any>,
        kProperty: KProperty1<EntityModel<D>, PropertyNullable<EntityModel<D>, *>>
    ) {
        val name = kProperty.name
        if (uProperty.name.isEmpty()
            && !name.isEmpty()
            && uProperty is PropertyNullableImpl
        ) {
            uProperty.name = name
        }
    }

    /** Assign a property setter to the uProperty */
    fun <D : Any> assignSetter(uProperty: PropertyNullable<D, Any>) {
        val eProperty = uProperty.entityClass.memberProperties.find { it.name == uProperty.name }
        if (eProperty is KMutableProperty<*>) when (uProperty) {
            is PropertyNullableImpl -> {
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
    fun <V : Any> set(property: PropertyNullable<D, V>, value: Any?): EntityBuilder<D> {
        map[property.name] = value
        return this
    }

    /** Create new object by a constructor (for immutable objects) */
    fun build(): D {
        val constructor = model._entityClass.constructors
            .stream()
            .filter { c -> c.parameters.size == map.size }
            .findFirst()
            .orElseThrow { IllegalStateException("No constructor[${map.size}] found") }
        val params = constructor.parameters
            .stream()
            .map { kParam -> map[kParam.name] }
            .toArray()
        return constructor.call(*params)
    }

    override fun toString() = model._entityClass.simpleName ?: "?"
}

/** See: https://stackoverflow.com/questions/44038721/constants-in-kotlin-whats-a-recommended-way-to-create-them */
object Constants {
    /** Undefined property setter */
    val UNDEFINED_SETTER: (d: Any, v: Any?) -> Unit = { d, v -> throw UnsupportedOperationException("read-only") }

}