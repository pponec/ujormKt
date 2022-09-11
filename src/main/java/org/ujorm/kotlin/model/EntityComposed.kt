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
    private val metadata: PropertyMetadata<D, D>

    constructor(domainClass: KClass<D>) {
        this.metadata = PropertyMetadataImpl(0U, "", domainClass, domainClass)
    }

    override fun data(): PropertyMetadata<D, D> = metadata

    override fun get(entity: D): D? = throw UnsupportedOperationException("Dummy object")

    override fun set(entity: D, value: D?) = throw UnsupportedOperationException("Dummy object")
}

/** Composed Entity  model */
open class DomainEntityModel<D : Any, V : Any> : PropertyNullable<D, V> {

    val prefixProperty: PropertyNullable<D, V>
    val originalEntityModel: EntityModel<V>
    val baseInstance get() = prefixProperty is SelfProperty<*>

    protected constructor(
        prefixProperty: PropertyNullable<D, V>,
        originalEntityModel: EntityModel<V>,
    ) {
        this.prefixProperty = prefixProperty
        this.originalEntityModel = originalEntityModel
    }

    protected constructor(originalEntityModel: EntityModel<V>) :
            this(SelfProperty(originalEntityModel.utils().entityClass) as PropertyNullable<D, V>, originalEntityModel)

    companion object {
        /** Primary factory method */
        fun <V : Any> of(originalEntityModel: EntityModel<V>): DomainEntityModel<V, V> {
            val selfProperty = SelfProperty(originalEntityModel.utils().entityClass)
            return DomainEntityModel(selfProperty, originalEntityModel)
        }

        /** Secondary factory method */
        fun <D : Any, M : Any, V : Any> of(
            leadProperty: PropertyNullable<D, M>,
            domainEntityModel: DomainEntityModel<M, V>,
        ): DomainEntityModel<D, V> {
            val headProperty = leadProperty + domainEntityModel
            return DomainEntityModel(headProperty, domainEntityModel.originalEntityModel)
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

    fun close(): DomainEntityModel<D, V> {
        // TODO ...
        return this
    }
}

//abstract open class DomainEntityModel<D : Any> : ComposedEntityModel<D, D> {
//
//    constructor(originalEntityModel: EntityModel<D>) : super(
//        SelfProperty(originalEntityModel.utils().entityClass),
//        originalEntityModel,
//        )
//
//}