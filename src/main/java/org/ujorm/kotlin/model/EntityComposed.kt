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
package org.ujorm.kotlin.model

import org.ujorm.kotlin.core.*
import kotlin.reflect.KClass

/** Dummy property */
class SelfProperty<D : Any> : PropertyNullable<D, D> {
    private val metadata : PropertyMetadata<D, D>

    constructor(domainClass: KClass<D>) {
        this.metadata = PropertyMetadataImpl(0U, "", domainClass, domainClass)
    }

    override fun data(): PropertyMetadata<D, D> = metadata

    override fun get(entity: D): D? = throw UnsupportedOperationException("Dummy object")

    override fun set(entity: D, value: D?) = throw UnsupportedOperationException("Dummy object")
}

/** Entity composed model */
open class ComposedEntityModel<D : Any, V : Any> : PropertyNullable<D, V> {

    val prefixProperty : PropertyNullable<D, V>?
    val originalEntityModel : EntityModel<V>
    val baseInstance get() = prefixProperty == null

    protected constructor(
        prefixProperty: PropertyNullable<D, V>?,
        originalEntityModel: EntityModel<V>,
    ) {
        this.prefixProperty = prefixProperty
        this.originalEntityModel = originalEntityModel
    }

    companion object {
        /** Primary factory method */
        fun <V: Any> of(originalEntityModel : EntityModel<V>) : ComposedEntityModel<V, V> {
            val selfProperty = SelfProperty(originalEntityModel.utils().entityClass)
            return ComposedEntityModel(null, originalEntityModel)
        }

        /** Secondary factory method */
        fun <D: Any, M: Any, V: Any> of(
            leadProperty: PropertyNullable<D, M>,
            composedEntityModel : ComposedEntityModel<M, V>,
        ) : ComposedEntityModel<D, V> {
            val headProperty = leadProperty + composedEntityModel
            return ComposedEntityModel(headProperty, composedEntityModel.originalEntityModel)
        }
    }

    override fun data(): PropertyMetadata<D, V> {
        return prefixProperty.data()
    }

    override fun get(entity: D): V? {
        return prefixProperty[entity]
    }

    override fun set(entity: D, value: V?) {
        prefixProperty[entity] = value
    }

    fun close() : ComposedEntityModel<D, V> {
        // TODO ...
        return this
    }
}

abstract open class DomainEntityModel<D : Any> : ComposedEntityModel<D, D> {

    constructor(originalEntityModel: EntityModel<D>) : super(
        SelfProperty(originalEntityModel.utils().entityClass),
        originalEntityModel,
        true)

}