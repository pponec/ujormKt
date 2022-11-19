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

import org.ujorm.kotlin.core.Constants.CLOSED_MESSAGE
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.*
import java.util.stream.Stream
import kotlin.collections.HashMap
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
    /** Is the property composed? */
    fun isComposed() = index == UByte.MAX_VALUE
    fun indexToInt() = index.toInt()

    /** State of properties */
    fun status(): String
}

/** API of the property descriptor for a nullable values */
class PropertyMetadataImpl<D : Any, V : Any> (
    override val index: UByte,
    name: String = "",
    internal val entityModel: BriefEntityModel<D>,
    override val valueClass: KClass<V>, // KClass<out V>
    override val readOnly: Boolean = false,
    /** Is the value nullable or required ? */
    override val nullable: Boolean = false,
) : PropertyMetadata<D, V> {
    override val entityClass: KClass<D> get() = entityModel.entityClass
    override val entityType : ClassType  get() = entityModel.entityType

    override var name: String = name
        internal set(value) {
            field = if (entityModel.open) value
            else throw IllegalStateException(CLOSED_MESSAGE)
        }

    /** Compare three attributes: 'entity class', 'entity alias' and 'property name'. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as PropertyMetadataImpl<*, *>
        if (entityModel != other.entityModel) return false
        if (name != other.name) return false

        return true
    }

    /** HashCode */
    override fun hashCode(): Int = Objects.hash(entityModel, name)

    override fun toString(): String =
        "${this::class.simpleName} {${status()}}"

    /** State of properties */
    override fun status(): String = "index=$index" +
            ", name=$name" +
            ", entityClass=${entityClass.simpleName}" +
            ", entityAlias=${entityModel.entityAlias}" +
            ", valueClass=${valueClass.simpleName}" +
            ", required=$required" +
            ", readOnly=$readOnly" +
            ", closed=${entityModel.closed}"
}

/** API of the property descriptor for a nullable values. */
interface PropertyNullable<D : Any, V : Any> : CharSequence {
    fun data() : PropertyMetadata<D, V>

    /** An entity alias name where a blank text means the default value. */
    fun entityAlias() : String

    /** Clone this property with a new alias */
    fun entityAlias(entityAlias: String) : PropertyNullable<D, V>

    /** Clone this property with a new alias */
    operator fun invoke(entityAlias: String): PropertyNullable<D, V> = entityAlias(entityAlias)

    /** Ascending sort request */
    fun asc() : SortingProperty<D, V> = SortingProperty(this, true)
    /** Descending sort request */
    fun desc() : SortingProperty<D, V> = SortingProperty(this, false)

    /** Get a value from the entity */
    operator fun get(entity: D): V?

    /** Set a value to the entity */
    operator fun set(entity: D, value: V?)

    /** Get a value from the entity */
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

/** API of the property descriptor */
interface Property<D : Any, V : Any> : PropertyNullable<D, V> {
    override operator fun get(entity: D): V

    /** Get a value from the entity */
    @Suppress("UNCHECKED_CAST")
    override operator fun get(entity: Array<Any?>): V = entity[data().indexToInt()] as V

    /** Create new composite property */
    operator fun <N: Any> plus(nextProperty : Property<V, N>) : Property<D, N> =
        ComposedPropertyImpl(this, nextProperty)

    /** Clone this property with a new alias */
    override fun entityAlias(entityAlias : String) : Property<D, V>

    /** Clone this property with a new alias */
    override operator fun invoke(entityAlias : String): Property<D, V> = entityAlias(entityAlias)
}

/** An implementation of the property descriptor for nullable values */
open class PropertyNullableImpl<D : Any, V : Any> internal constructor(
    internal val metadata: PropertyMetadataImpl<D, V>,
    /** Value provider is not the part of API */
    internal open val getter: (D) -> V?,
    /** Value writer is not the part of API */
    setter: (D, V?) -> Unit,
) : PropertyNullable<D, V> {

    private val hashCode : Int by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Objects.hash(metadata.entityClass, entityAlias(), name())
    }

    override fun entityAlias(): String = metadata.entityModel.entityAlias

    /** Value writer is not the part of API */
    internal open var setter: (D, V?) -> Unit = setter
        set(value) {
            field = if (metadata.entityModel.open) value
            else throw IllegalStateException(CLOSED_MESSAGE)
        }

    override fun get(entity: D): V? = getter.invoke(entity)
    override fun set(entity: D, value: V?) = setter.invoke(entity, value)
    final override fun data() = metadata

    /** Clone this property for a new alias */
    override fun entityAlias(entityAlias : String) : PropertyNullable<D, V> =
        object : PropertyNullableImpl<D, V>(metadata, getter, setter) {
           override fun entityAlias(): String = entityAlias
        }

    /** Call the {@sample info()} method */
    override fun toString(): String {
        return info()
    }

    /** Compare three attributes: 'entity class', 'entity alias' and 'property name'. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as PropertyNullable<*, *>
        if (metadata.entityModel.entityClass != other.data().entityClass) return false
        if (entityAlias() != other.entityAlias()) return false // Property can be overwritten
        if (name() != other.name()) return false

        return true
    }

    override fun hashCode(): Int = hashCode
}

/** An implementation of the property descriptor */
open class PropertyImpl<D : Any, V : Any> : Property<D, V>, PropertyNullableImpl<D, V> {
    internal constructor(
        metadata: PropertyMetadataImpl<D, V>,
        getter: (D) -> V,
        setter: (D, V?) -> Unit,
    ) : super(metadata, getter, setter)

    override val getter: (D) -> V = super.getter as (D) -> V
    override fun set(entity: D, value: V?) = setter.invoke(entity, value
        ?: throw IllegalArgumentException("Notnull value is expected")
    )

    final override fun get(entity: D): V = getter.invoke(entity)

    /** Clone this property for a new alias */
    override fun entityAlias(entityAlias : String) : Property<D, V> =
        object : PropertyImpl<D, V>(metadata, getter, setter) {
            override fun entityAlias(): String = entityAlias
        }
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

class EntityProviderUtils {
    private var locked: Boolean = false

    /** Get all entity models */
    private var entityModels: MutableList<EntityModel<*>> = mutableListOf()

    private var entityMap : Map<KClass<*>, EntityModel<*>> = emptyMap()

    /** Add an entity and close */
    fun <D : Any, E : EntityModel<D>> add(entity : E) : E {
        check(!locked) { "The object is locked" }
        entityModels.add(entity.close())
        return entity
    }

    fun entityModels() : List<EntityModel<*>> = entityModels

    /** Lock the model if it hasn't already. */
    fun close(entities : AbstractEntityProvider) {
        if (locked) return

        if (entityModels.isEmpty()) {
            Reflections(EntityModel::class).findMemberExtensionObjectOfPackage(entities::class.java.packageName, entities)
                .forEach {
                    it.closeModel()
                    entityModels.add(it)
                }
        }
        println(">>>" + entityModels.size)

        var map = HashMap<KClass<*>, EntityModel<*>>(this.entityModels.size)
        entityModels.forEach {
            it.closeModel()
            map[it.utils().entityClass] = it
        }
        entityMap = map
        entityModels = Collections.unmodifiableList(entityModels) as MutableList<EntityModel<*>>
        locked = true
    }
}

/** Interface of the domain metamodel */
abstract class AbstractEntityProvider {

    private val utils = EntityProviderUtils()

    fun utils() = utils

    /** Register a new entity */
    fun <D : Any, E : EntityModel<D>> add(entity : E) : E = utils.add(entity)

    @Suppress("UNCHECKED_CAST")
    open fun <R : AbstractEntityProvider> close() : R {
        utils.close(this)
        return this as R
    }
}

/** Brief entity model */
class BriefEntityModel<D : Any>(
    val entityClass: KClass<D>,
) {
    val entityType = ClassType.of(entityClass)

    var entityAlias: String = ""
        internal set(value) {
            field = if (open) value
            else throw IllegalStateException(CLOSED_MESSAGE)
        }

    var closed = false
        internal set(value) {
            field = if (open) value
            else throw IllegalStateException(CLOSED_MESSAGE)
        }

    val open: Boolean = !closed

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BriefEntityModel<*>

        if (entityClass != other.entityClass) return false
        if (entityAlias != other.entityAlias) return false

        return true
    }

    override fun hashCode(): Int =
        entityClass.hashCode() * 37 + entityAlias.hashCode()

    override fun toString(): String =
        "${entityClass.simpleName}($entityAlias)"
}

/** Utility for building entity properties */
class EntityUtils<D : Any>(
    /** Entity metamodel */
    val entityModel: EntityModel<D>,
    /** A brief domain model */
    val briefModel: BriefEntityModel<D>,
) {
    /** Get the main domain class */
    val entityClass : KClass<D> get() = briefModel.entityClass
    /** Get an entity class type */
    val entityType : ClassType get() = briefModel.entityType

    var properties: List<PropertyNullable<D, Any>> = mutableListOf()
        private set(value) { field = if (briefModel.open) value
        else throw java.lang.IllegalStateException(CLOSED_MESSAGE)}
    val size get() = properties.size

    /** Column name style */
    internal var columnNameStyle: Boolean? = null
        set(value) { field = if (columnNameStyle == null || columnNameStyle == value) value
            else throw IllegalArgumentException("One column style per one entity is allowed")
        }
    val columnStyle: Boolean = columnNameStyle ?: false

    /** Mapping a property name to its property */
    private lateinit var map : Map<String, PropertyNullable<D, *>>

    /** Initialize and close the entity model. */
    fun close() : EntityModel<D> {
        if (!briefModel.closed) {
            properties = Collections.unmodifiableList(properties)
            val kPropertyArray = Utils.getKPropertyMap(entityModel)
            for (p in properties) {
                val kProperty = kPropertyArray[p.data().index.toShort()]
                    ?: throw IllegalStateException("Property not found: ${p.data().entityClass.simpleName}.${p.data().index}")
                Utils.assignName(p, kProperty)
                Utils.assignSetter(p, kProperty)
            }
            map = buildPropertyMap()
            briefModel.closed = true
        }
        return entityModel
    }

    /** Validate unique property name and build a name map */
    private fun buildPropertyMap() : Map<String, PropertyNullable<D, *>> {
        val result = HashMap<String, PropertyNullable<D, *>>(properties.size)
        properties.forEach{
            val previous = result.put( it.name(), it)
            check(previous == null) { "The attribute name is occupied: $it" }
        }
        return result
    }

    /** Find property by its name or throw an exception */
    fun findProperty(name: String): PropertyNullable<D, *> =
        map.get(name) ?: throw NoSuchElementException(
            "For: ${entityModel.utils().entityClass.simpleName}.$name")

    /** Create new not-null Property model using the method with all necessary parameters. */
    fun <V : Any> createProperty(
        valueClass: KClass<V>,
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        name: String = "",
    ): Property<D, V> {
        val propertyMetadata = PropertyMetadataImpl(properties.size.toUByte(),
            name, briefModel, valueClass, false, false)
        val result = PropertyImpl(propertyMetadata, getter, setter)
        addToList(result)
        return result
    }

    /** Creates new not-null Property model using the method with all necessary parameters. */
    fun <V : Any> createPropertyNullable(
        valueClass: KClass<V>,
        getter: (D) -> V?,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER,
        name: String = ""
    ): PropertyNullable<D, V> {
        val propertyMetadata = PropertyMetadataImpl(properties.size.toUByte(),
            name, briefModel, valueClass, false, true)
        val result = PropertyNullableImpl(propertyMetadata, getter, setter)
        addToList(result)
        return result
    }

    protected fun <V : Any> createPropertyRaw(valueClass: KClass<V>, any: Any): Property<RawEntity<D>, V> {
        TODO()
    }

    /** Add property to the list */
    fun addToList(item: PropertyNullable<D, *>) {
        if (briefModel.closed) throw IllegalStateException(CLOSED_MESSAGE)
        val _list = properties as MutableList<PropertyNullable<D, *>>
        _list.add(item)
    }

    override fun toString() = "${entityClass.simpleName ?: ""}[${size}]"
}

/** Implementations of the EntityModels cam be generated in the feature. */
abstract class EntityModel<D : Any>(entityClass: KClass<D>) {
    /** Property builder properties */
    protected val propertyBuilder = EntityUtils(this, BriefEntityModel(entityClass))

    /** The provider must be a method because the entity attributes are reserved for the Entity model. */
    fun utils() = propertyBuilder

    /** Initialize and close the entity model and return yourself back. */
    @Suppress("UNCHECKED_CAST")
    fun <R : EntityModel<D>> close(): R = propertyBuilder.close() as R

    /** Initialize and close the entity model. */
    fun closeModel() : Unit {
         propertyBuilder.close()
    }

    /** Clone the metamodel for required alias.
     * Use the method judiciously because it needs certain system resources. */
    @Suppress("UNCHECKED_CAST")
    fun <R : EntityModel<D>> alias(alias: String): R {
        val result = javaClass.getConstructor().newInstance() as R
        result.propertyBuilder.briefModel.entityAlias = alias
        return result.close()
    }

    /** Check the value and return the one. */
    protected fun hasLength(value : String, name : String = "name") : String {
        require(!value.isBlank()) { "$name is required" }
        return value
    }

    override fun toString() = utils().toString()

    // --- Common properties builder ---

    /** Create a non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    protected inline fun <reified V : Any> property(
        noinline getter: (D) -> V
    ) = propertyBuilder.createProperty(V::class, getter)

    /** Create a non-null property by a class.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    protected inline fun <reified V : Any> property(valueClass: KClass<V>) : Property<RawEntity<D>, V> = TODO()
        //propertyBuilder.createPropertyRaw(valueClass, TODO())

    /** Create a non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Deprecated("Unsupported method")
    private inline fun <reified V : Any> property(
        name: String,
        noinline getter: (D) -> V
    ) = propertyBuilder.createProperty(V::class, getter, name = hasLength(name))

    /** Create new non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    protected inline fun <reified V : Any> propertyNullable(
        noinline getter: (D) -> V?
    ) = propertyBuilder.createPropertyNullable(V::class, getter)

    /** Create new non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Deprecated("Unsupported method")
    private inline fun <reified V : Any> propertyNullable(
        name: String,
        noinline getter: (D) -> V?,
    ) = propertyBuilder.createPropertyNullable(V::class, getter, name = hasLength(name))

    /** Create new Array */
    fun createArray(): Array<Any?> = arrayOfNulls(propertyBuilder.size)

    /** Create new instance of the domain object */
    @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun new(): D {
        val entityClass = propertyBuilder.entityClass.java
        val myHandler = RawEntity(propertyBuilder.entityModel)
        val result = Proxy.newProxyInstance(
            entityClass.classLoader,
            arrayOf<Class<*>>(entityClass, AbstractEntity::class.java),
            myHandler
        )
        return result as D
    }

    /** Create new instance of the domain object and assign properties */
    inline fun new(init: D.() -> Unit) : D = new().also { it.init() }
}

/** Common utilities */
internal object Utils {
    /** Get all properties of the instance for a required types */
    @Suppress("UNCHECKED_CAST")
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

    /** Get a maps: index to KProperty1. Composite property has index {@sample -1}, if any. */
    @Suppress("UNCHECKED_CAST")
    fun <D : Any> getKPropertyMap(domainObject: D): Map<Short, KProperty1<D, PropertyNullable<D, *>>> =
        getKProperties(domainObject, PropertyNullable::class)
            .toList()
            .map {
                val instance = it(domainObject)
                val index = if (instance is PropertyNullableImpl) instance.metadata.index.toShort() else -1
                index to it as KProperty1<D, PropertyNullable<D, *>>
            }
            .toMap()

    /** Assign a property name to the uProperty */
    fun <D : Any> assignName(
        /** Ujorm property */
        uProperty: PropertyNullable<D, Any>,
        /** Kotlin property */
        kProperty: KProperty1<EntityModel<D>, PropertyNullable<EntityModel<D>, *>>,
    ) {
        val name = kProperty.name
        if (uProperty.data().name.isEmpty()
            && name.isNotEmpty()
            && uProperty is PropertyNullableImpl
        ) {
            uProperty.data().name = name
        }
    }

    /** Assign a property setter to the uProperty */
    fun <D : Any> assignSetter(
        /** Ujorm property */
        uProperty: PropertyNullable<D, Any>,
        /** Kotlin property */
        kProperty: KProperty1<EntityModel<D>, PropertyNullable<EntityModel<D>, *>>,
    ) {
        val eProperty = uProperty.data().entityClass.memberProperties.find { it.name == kProperty.name }
        if (eProperty is KMutableProperty<*>) when (uProperty) {
            is PropertyNullableImpl -> {
                if (uProperty.setter === Constants.UNDEFINED_SETTER)
                    uProperty.setter = { d, v -> eProperty.setter.call(d, v) }
            }
        }
    }
}

/** Sorting property */
class SortingProperty<D : Any, V : Any> (
    val property : PropertyNullable<D, V>,
    val asc : Boolean)

/** See: https://stackoverflow.com/questions/44038721/constants-in-kotlin-whats-a-recommended-way-to-create-them */
object Constants {
    /** Undefined property setter */
    val UNDEFINED_SETTER: (d: Any, v: Any?) -> Unit = { d, v ->
        TODO("read-only")
    }
    const val CLOSED_MESSAGE = "Object is closed"
}

class ComposedPropertyMetadata<D : Any, M : Any, V : Any>(
    val primaryProperty: PropertyNullable<D, M>,
    val secondaryProperty: PropertyNullable<M, V>
) : PropertyMetadata<D, V> {

    override val index: UByte get() = UByte.MAX_VALUE
    override val name: String by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        "${primaryProperty.data().name}.${secondaryProperty.data().name}" }
    override val entityClass: KClass<D> get() = primaryProperty.data().entityClass
    override val entityType: ClassType get() = primaryProperty.data().entityType
    override val valueClass: KClass<V> get() = secondaryProperty.data().valueClass
    override val readOnly = primaryProperty.data().readOnly || secondaryProperty.data().readOnly
    override val nullable = primaryProperty.data().nullable || secondaryProperty.data().nullable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComposedPropertyMetadata<*, *, *>

        if (primaryProperty != other.primaryProperty) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return primaryProperty.hashCode() * 37 + name.hashCode()
    }

    override fun status(): String = "index=$index" +
            ", name=$name" +
            ", entityClass=${entityClass.simpleName}" +
            ", valueClass=${valueClass.simpleName}" +
            ", required=$required" +
            ", readOnly=$readOnly"

    override fun toString(): String {
        return "ComposedPropertyMetadata {${status()}}"
    }
}

/** Composed nullable property implementation */
open class ComposedPropertyNullableImpl<D : Any, M : Any, V : Any> : PropertyNullable<D, V> {
    protected val metadata: ComposedPropertyMetadata<D, M, V>

    constructor(
        leftProperty : PropertyNullable<D, M>,
        righProperty : PropertyNullable<M, V>
    ) {
        this.metadata = ComposedPropertyMetadata(leftProperty, righProperty)
    }

    override fun data() = this.metadata

    override fun entityAlias(): String = metadata.primaryProperty.entityAlias()

    @Deprecated("Method is not supported for composed properties")
    override fun entityAlias(entityAlias: String): PropertyNullable<D, V> {
        TODO("Method is not supported for composed properties")
    }

    override fun get(entity: D): V? {
        val entity2 = metadata.primaryProperty[entity]
        return if (entity2 != null) metadata.secondaryProperty[entity2] else null
    }

    override fun set(entity: D, value: V?) {
        val entity2 = metadata.primaryProperty[entity]
            ?: throw IllegalArgumentException("Value of property ${info()} is null")
        metadata.secondaryProperty.set(entity2, value)
    }

    override fun toString(): String {
        return metadata.name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PropertyNullable<*, *>
        if (metadata != other.data()) return false
        return true
    }

    override fun hashCode(): Int =
        metadata.hashCode()
}

/** Composed non-null property implementation */
class ComposedPropertyImpl<D : Any, M : Any, V : Any> : Property<D, V>, ComposedPropertyNullableImpl<D, M, V> {
    constructor(
        leftProperty: Property<D, M>,
        rightProperty: Property<M, V>
    ) : super(leftProperty, rightProperty)

    override fun get(entity: D): V = super<ComposedPropertyNullableImpl>.get(entity)
        ?: throw IllegalArgumentException("Value of property ${info()} is null")

    @Deprecated("Method is not supported for composed properties")
    override fun entityAlias(entityAlias: String): Property<D, V> {
        TODO("Method is not supported for composed properties")
    }
}