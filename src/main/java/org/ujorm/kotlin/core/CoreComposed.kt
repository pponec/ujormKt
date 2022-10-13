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
package org.ujorm.kotlin.coreComposed

import org.ujorm.kotlin.core.*
import org.ujorm.kotlin.core.RawEntity
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

/** Dummy property */
class SelfProperty<D : Any> : PropertyNullable<D, D> {
    private val metadata: PropertyMetadata<D, D>

    constructor(domainClass: KClass<D>) {
        this.metadata = PropertyMetadataImpl(0U, "", BriefEntityModel(domainClass), domainClass)
    }

    override fun data(): PropertyMetadata<D, D> = metadata

    override fun entityAlias(): String = ""

    override fun get(entity: D): D? = TODO("Dummy object")

    override fun set(entity: D, value: D?) = throw UnsupportedOperationException("Dummy object")

    override fun entityAlias(entityAlias: String): PropertyNullable<D, D> {
        TODO("Not yet implemented")
    }
}

/** Composed Entity model */
abstract class DomainEntityModel<D : Any, V : Any> : PropertyNullable<D, V> {

    /** Original Entity model with direct properties */
    abstract protected val core: EntityModel<V>

    /** Composed property to the entity model. */
    val domainProperty: PropertyNullable<D, V>

    /** Entity model with direct properties. */
    val originalEntityModel: EntityModel<V>

    /** Is it the primary instance? */
    val baseInstance get() = domainProperty is SelfProperty<*>

    protected constructor(
        prefixProperty: PropertyNullable<D, V>,
        originalEntityModel: EntityModel<V>,
    ) {
        this.domainProperty = prefixProperty
        this.originalEntityModel = originalEntityModel
    }

    protected constructor() {
        this.domainProperty = SelfProperty(core.utils().entityClass) as PropertyNullable<D, V>
        this.originalEntityModel = core
    }

    override fun data(): PropertyMetadata<D, V> {
        return domainProperty.data()
    }

    override fun entityAlias(): String = ""

    override fun get(entity: D): V? {
        return domainProperty[entity]
    }

    override fun set(entity: D, value: V?) {
        domainProperty[entity] = value
    }

    /** Build the new Property */
    protected fun <M : Any, V : Any, R : PropertyNullable<D, V>> property(property : PropertyNullable<M, V>) : R {
        return TODO()
    }

    fun close(): DomainEntityModel<D, V> {
        // TODO ...
        return this
    }

    override fun entityAlias(entityAlias: String): PropertyNullable<D, V> {
        TODO("Not yet implemented")
    }

    /** Create new instance of the domain object */
    @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun new(): D {
        require(core.utils().entityType == ClassType.INTERFACE) { "Only interface is supported" }

        val entityClass = core.utils().entityClass.java
        val myHandler = RawEntity(core.utils().entityModel)
        val result = Proxy.newProxyInstance(
            entityClass.classLoader, arrayOf<Class<*>>(entityClass),
            myHandler
        )
        @Suppress("UNCHECKED_CAST")
        return result as D
    }

    /** Create new instance of the domain object and assign properties */
    inline fun new(init: D.() -> Unit) : D = new().also { it.init() }

    fun closeModel() {
        // TODO
    }
}

/** Interface of the domain metamodel */
abstract class AbstractDomainEntityProvider {

    private val utils = DomainEntityProviderUtils()

    fun utils() = utils

    /** Register a new entity */
    fun <D : Any, E : DomainEntityModel<D, Any>> add(entity : E) : E = utils.add(entity)

    @Suppress("UNCHECKED_CAST")
    open fun <R : AbstractEntityProvider> close() : R {
        utils.close(this)
        return this as R
    }
}

class DomainEntityProviderUtils {
    private var locked: Boolean = false

    /** Get all entity models */
    private var entityModels: MutableList<DomainEntityModel<*, *>> = mutableListOf()

    private var entityMap : Map<KClass<*>, DomainEntityModel<*, *>> = emptyMap()

    /** Add an entity and close */
    fun <D : Any, E : DomainEntityModel<D, Any>> add(entity : E) : E {
        check(!locked) { "The object is locked" }
        entityModels.add(entity.close())
        return entity
    }

    fun entityModels() : List<DomainEntityModel<*, *>> = entityModels

    /** Lock the model if it hasn't already. */
    fun close(entities : AbstractDomainEntityProvider) {
        if (locked) return

        if (entityModels.isEmpty()) {
            Reflections(DomainEntityModel::class)
                .findMemberExtensionObjectOfPackage(entities::class.java.packageName, entities)
                .forEach {
                    it.close()
                    entityModels.add(it)
                }
        }
        println(">>>" + entityModels.size)

        var map = HashMap<KClass<*>, DomainEntityModel<*, *>>(this.entityModels.size)
        entityModels.forEach {
            it.closeModel()
            map[it.domainProperty.data().entityClass] = it
        }
        entityMap = map
        entityModels = Collections.unmodifiableList(entityModels)
        locked = true
    }
}
