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
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

/** API to provide nullable values */
interface PropertyNullableAccessor<D : Any, V : Any> {
    /** Get Nullable value */
    fun getNullable(entity: D): V?

}

/** API of the property descriptor for a nullable values */
open class PropertyMetadataImpl<D : Any, V : Any> (
    override val index: UByte,
    name: String = "",
    internal val entityModel: BriefEntityModel<D>,
    override val valueClass: KClass<V>, // KClass<out V>
    override val readOnly: Boolean = false,
    /** Is the value nullable or required ? */
    override val nullable: Boolean = false,
) : PropertyMetadata<D, V> {
    override val entityClass: KClass<D> get() = entityModel.entityClass
    override val entityType : ClassType get() = entityModel.entityType
    override val level: UByte get() = 1U
    override var name: String = name
        internal set(value) {
            field = if (entityModel.open) value
            else throw IllegalStateException(CLOSED_MESSAGE)
        }

    /** Returns true if the property class is the same or subtype of the parameter class. */
    fun isTypeOf(clazz: KClass<*>) = valueClass.isSubclassOf(clazz)

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

/** API of the property descriptor for a List item values.
 * See: https://stackoverflow.com/questions/51800653/how-to-get-kclass-of-generic-classes-like-baseresponseiterableuser */
open class ListPropertyMetadataImpl<D : Any, V : Any> : PropertyMetadataImpl<D, List<V>> {

    val itemClass: KClass<V> // KClass<out V>

    constructor(
        index: UByte,
        name: String,
        entityModel: BriefEntityModel<D>,
        itemClass: KClass<V>,
        readOnly: Boolean,
        nullable: Boolean,
    ) : super(index, name, entityModel,
        List::class as KClass<List<V>>,
        readOnly,
        nullable) {
        this.itemClass = itemClass
    }
}

/** An implementation of the direct property descriptor for nullable values */
open class PropertyNullableImpl<D : Any, V : Any> internal constructor(
    internal val metadata: PropertyMetadataImpl<D, V>,
) : PropertyNullable<D, V>, PropertyNullableAccessor<D, V> {

    private val hashCode : Int by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Objects.hash(metadata.entityClass, entityAlias(), name())
    }

    override fun entityAlias(): String = metadata.entityModel.entityAlias

    override fun get(entity: D): V? = getNullable(entity)

    override fun getNullable(entity: D): V? =
        (entity as AbstractEntity<D>).`~~`().get(metadata)

    @Suppress("UNCHECKED_CAST")
    override fun set(entity: D, value: V?) =
        (entity as AbstractEntity<D>).`~~`().set(metadata, value)

    final override fun data() = metadata

    /** Clone this property for a new alias */
    override fun entityAlias(entityAlias : String) : PropertyNullable<D, V> =
        object : PropertyNullableImpl<D, V>(metadata) {
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

/** An implementation of the direct property descriptor */
open class PropertyImpl<D : Any, V : Any> : Property<D, V>, PropertyNullableImpl<D, V> {
    internal constructor(
        metadata: PropertyMetadataImpl<D, V>,
    ) : super(metadata)

    final override fun get(entity: D): V = getNullable(entity) as V

    /** Clone this property for a new alias */
    override fun entityAlias(entityAlias : String) : Property<D, V> =
        object : PropertyImpl<D, V>(metadata) {
            override fun entityAlias(): String = entityAlias
        }
}

/** Object maps Entity class to an Entity model. */
class EntityProviderUtils {

    @Volatile
    var closed: Boolean = false
        private set

    /** Get all entity models */
    private var entityModels: MutableList<EntityModel<*>> = mutableListOf()

    private var entityMap : Map<KClass<*>, EntityModel<*>> = emptyMap()

    /** Add all entities and close the it */
    fun <E : EntityModel<*>> addAll(vararg entities : E)  {
        entities.forEach { add(it) }
    }

    /** Add an entity and close it */
    internal fun <E : EntityModel<*>> add(entity : E) : E {
        check(!closed) { "The object is locked" }
        entityModels.add(entity.close())
        return entity
    }

    fun entityModels() : List<EntityModel<*>> = entityModels

    /** Close the model for further changes. */
    @Synchronized
    fun close(entities : AbstractEntityProvider) {
        if (closed) return

        if (entityModels.isEmpty()) {
            Reflections(EntityModel::class).findMemberExtensionObjectOfPackage(entities::class.java.packageName, entities)
                .forEach {
                    it.closeModel()
                    entityModels.add(it)
                }
        }

        var map = HashMap<KClass<*>, EntityModel<*>>(this.entityModels.size)
        entityModels.forEach {
            it.closeModel()
            map[it.utils().entityClass] = it
        }
        entityMap = map
        entityModels = Collections.unmodifiableList(entityModels) as MutableList<EntityModel<*>>
        closed = true
    }

    /** Find an entity model acording entity class */
    @Suppress("UNCHECKED_CAST")
    fun <D: Any> findEntityModel(entityClass: KClass<D>): EntityModel<D> =
        entityMap[entityClass] as EntityModel<D>

    /** Create a new relation */
    inline fun <D: Any, V: Any> newRelation(property: PropertyNullable<D, V>): V =
        findEntityModel(property.data().valueClass).new()

    /** Set a new value to any entity by the composed relation, where missing relation is created. */
    fun <D: Any, V: Any> setValueWithRelations(domain: D, value: V, property: PropertyNullable<D, V>) {
        val chainedProperty = ChainedProperty<D, V>(property, this)
        chainedProperty[domain] = value
    }

    /** Clone entity to the new object */
    fun  <D: Any> clone(entity: D): D {
        val source = entity as AbstractEntity<D>
        val src = source.`~~`()
        val model = src.model
        val result = model.new() as AbstractEntity<D>
        val dst = result.`~~`()

        model.utils().properties.forEach{
            val property = it.metadata as PropertyMetadata<D, Any>
            dst.set(property, src.get(property))
        }
        return result as D
    }
}

/** Interface of the domain metamodel */
abstract class AbstractEntityProvider {

    private val utils = EntityProviderUtils()

    /** Close the metamodel and get the utility object. */
    fun utils() : EntityProviderUtils {
        if (!utils.closed && !utils.entityModels().isEmpty()) {
            utils.close(this)
        }
        return utils
    }

    /** Register a new entity */
    fun <D : Any, E : EntityModel<D>> add(entity : E) : E = utils.add(entity)

    /** Initialize and close the entity model */
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

    var properties: List<PropertyNullableImpl<D, Any>> = mutableListOf()
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
    private lateinit var map : Map<String, PropertyNullableImpl<D, *>>

    /** Initialize and close the entity model. */
    fun close() : EntityModel<D> {
        if (!briefModel.closed) {
            properties = Collections.unmodifiableList(properties)
            val kPropertyArray = Utils.getKPropertyMap(entityModel)
            for (p in properties) {
                val kProperty = kPropertyArray[p.data().index.toShort()]
                    ?: throw IllegalStateException("Property not found: ${p.data().entityClass.simpleName}.${p.data().index}")
                Utils.assignName(p, kProperty)
            }
            map = buildPropertyMap()
            briefModel.closed = true
        }
        return entityModel
    }

    /** Validate unique property name and build a name map */
    private fun buildPropertyMap() : Map<String, PropertyNullableImpl<D, *>> {
        val result = HashMap<String, PropertyNullableImpl<D, *>>(properties.size)
        properties.forEach{
            val previous = result.put( it.name(), it)
            check(previous == null) { "The attribute name is occupied: $it" }
        }
        return result
    }

    /** Find property by its name or throw an exception */
    fun findProperty(name: String): PropertyNullableImpl<D, *> =
        map.get(name) ?: throw NoSuchElementException(
            "For: ${entityModel.utils().entityClass.simpleName}.$name")

    /** Create new not-null Property model using the method with all necessary parameters. */
    fun <V : Any> createProperty(
        valueClass: KClass<V>,
        name: String = "",
    ): Property<D, V> {
        val propertyMetadata = PropertyMetadataImpl(properties.size.toUByte(),
            name, briefModel, valueClass, readOnly = false, nullable = false)
        val result = PropertyImpl(propertyMetadata)
        addToList(result)
        return result
    }

    /** Create new not-null List Property model using the method with all necessary parameters. */
    fun <V : Any> createListProperty(
        valueClass: KClass<V>,
        name: String = "",
    ): Property<D, List<V>> {
        val propertyMetadata = ListPropertyMetadataImpl(properties.size.toUByte(),
            name, briefModel, valueClass, readOnly = false, nullable = false)
        val result = PropertyImpl(propertyMetadata)
        addToList(result)
        return result
    }

    /** Creates new not-null Property model using the method with all necessary parameters. */
    fun <V : Any> createPropertyNullable(
        valueClass: KClass<V>,
        name: String = ""
    ): PropertyNullable<D, V> {
        val propertyMetadata = PropertyMetadataImpl(properties.size.toUByte(),
            name, briefModel, valueClass, false, true)
        val result = PropertyNullableImpl(propertyMetadata)
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
    fun closeModel() {
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
    private fun hasLength(value : String, name : String = "name") : String {
        require(!value.isBlank()) { "$name is required" }
        return value
    }

    override fun toString() = utils().toString()

    // --- Common properties builder ---

    /** Create a non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Suppress("UNUSED_PARAMETER")
    protected inline fun <reified V : Any> property(getter: (D) -> V) =
        propertyBuilder.createProperty(V::class)

    protected inline fun <reified V : Any> property(valueClass: KClass<V>) : Property<RawEntity<D>, V> = TODO()
        //propertyBuilder.createPropertyRaw(valueClass, TODO())

    /** Create a non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Deprecated("Unsupported method")
    @Suppress("UNUSED_PARAMETER")
    private inline fun <reified V : Any> property(
        name: String,
        getter: (D) -> V
    ) = propertyBuilder.createProperty(V::class, name = hasLength(name))

    /** Create new non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Suppress("UNUSED_PARAMETER")
    protected inline fun <reified V : Any> propertyNullable(getter: (D) -> V?) =
        propertyBuilder.createPropertyNullable(V::class)

    @Deprecated("Method will be removed")
    internal inline fun <reified V : Any> propertyNullable(valueClass: KClass<V>
    ) = propertyBuilder.createProperty(valueClass)

    /** Create new non-null property.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Deprecated("Unsupported method")
    @Suppress("UNUSED_PARAMETER")
    private inline fun <reified V : Any> propertyNullable(
        name: String,
        getter: (D) -> V?,
    ) = propertyBuilder.createPropertyNullable(V::class, name = hasLength(name))

    /** Create a non-null property type of List.
     * NOTE: The field must heave the same as the original Entity, or use the same name by a name argument.
     */
    @Suppress("UNUSED_PARAMETER")
    internal inline fun <reified V : Any> propertyList(getter: () -> KProperty1<V, D>): Property<D, List<V>> {
        return propertyBuilder.createListProperty(V::class, "")
    }

    /** Create new Array */
    fun createArray(): Array<Any?> = arrayOfNulls(propertyBuilder.size)

    /** Create new instance of the domain object */
    @JvmName("newObject")
    @Suppress("UNCHECKED_CAST")
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
}

/** Sorting property */
class SortingProperty<D : Any, V : Any> (
    val property : PropertyNullable<D, V>,
    val asc : Boolean)

/** See: https://stackoverflow.com/questions/44038721/constants-in-kotlin-whats-a-recommended-way-to-create-them */
object Constants {
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
    override val level: UByte by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ( primaryProperty.data().level
        + secondaryProperty.data().level).toUByte()
    }

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
open class ComposedPropertyNullableImpl<D : Any, M : Any, V : Any> :
    PropertyNullable<D, V>,
    PropertyNullableAccessor<D, V>
{
    protected val metadata: ComposedPropertyMetadata<D, M, V>

    constructor(
        primaryProperty : PropertyNullable<D, M>,
        secondaryProperty : PropertyNullable<M, V>
    ) {
        this.metadata = ComposedPropertyMetadata(primaryProperty, secondaryProperty)
    }

    override fun data() = this.metadata

    override fun entityAlias(): String = metadata.primaryProperty.entityAlias()

    @Deprecated("Method is not supported for composed properties")
    override fun entityAlias(entityAlias: String): PropertyNullable<D, V> {
        TODO("Method is not supported for composed properties")
    }

    override fun get(entity: D): V? = getNullable(entity)

    override fun getNullable(entity: D): V? {
        val middleObject : M? = (metadata.primaryProperty as PropertyNullableAccessor<D, M>)
            .getNullable(entity)
        return if (middleObject != null) (metadata.secondaryProperty as PropertyNullableAccessor<M, V>)
            .getNullable(middleObject)
        else null
    }

    override fun set(entity: D, value: V?) {
        val middleObject : M? = (metadata.primaryProperty as PropertyNullableAccessor<D, M>).getNullable(entity)
        if (middleObject != null) {
            metadata.secondaryProperty.set(middleObject, value)
        } else {
            throw java.lang.IllegalStateException("Relation ${metadata.primaryProperty} is null")
        }
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

    override fun get(entity: D): V = getNullable(entity)
        ?: throw IllegalArgumentException("Value of property ${info()} is null")

    @Deprecated("Method is not supported for composed properties")
    override fun entityAlias(entityAlias: String): Property<D, V> {
        TODO("Method is not supported for composed properties")
    }
}

/** This property implementation allows writing values to entity -  including creation of missing relations. */
class ChainedProperty<D : Any, V : Any> {
    private val utils: EntityProviderUtils
    private val properties: Array<PropertyNullableImpl<Any, Any>?>
    private var idx = 0

    constructor(property: PropertyNullable<D, V>, utils: EntityProviderUtils) {
        this.utils = utils
        this.properties = arrayOfNulls(property.data().level.toInt())
        add(property)
    }

    fun add(property: PropertyNullable<*, *>) {
        if (property is ComposedPropertyNullableImpl<*, *, *>) {
            val data = property.data()
            add(data.primaryProperty)
            add(data.secondaryProperty)
        } else {
            properties[idx++] = property as PropertyNullableImpl<Any, Any>
        }
    }

    /** Set value to a domain including missing relations. */
    operator fun set(domain: D, value: V?) {
        var myDomain: Any = domain
        for (i in 0..properties.size - 2) {
            val p = properties[i] ?: throw IllegalStateException("properties[$i]")
            var relation = p.getNullable(myDomain)
            if (relation == null) {
                relation = utils.newRelation(p)
                p.set(myDomain, relation)
            }
            myDomain = relation
        }
        properties.last()!!.set(myDomain, value)
    }

    /** Get count of the property items */
    fun size() = idx

    override fun toString(): String {
        val result = StringBuilder()
        properties.forEachIndexed { i, p ->
            val metadata = p?.metadata ?: throw IllegalStateException("properties[$i]")
            if (i == 0) {
                result.append(metadata.entityModel.entityClass.simpleName).append(": ")
            } else {
                result.append('.')
            }
            result.append(metadata.name)
        }
        return result.toString()
    }
}