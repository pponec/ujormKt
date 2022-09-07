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
/*abstract*/ class EntityComposedModel<D : Any, V : Any> : PropertyNullable<D, V> {

    val composedProperty : PropertyNullable<D, V>
    val originalEntityModel : EntityModel<V>
    val baseInstance : Boolean

    private constructor(
        headProperty: PropertyNullable<D, V>,
        originalEntityModel: EntityModel<V>,
        baseInstance: Boolean = headProperty is SelfProperty<*>,
    ) {
        this.composedProperty = headProperty
        this.originalEntityModel = originalEntityModel
        this.baseInstance = baseInstance
    }

    companion object {
        /** Factory method */
        fun <D: Any, V: Any> of(
            headProperty: PropertyNullable<D, V>,
            originalEntityModel : EntityModel<V>,
        ) : EntityComposedModel<D, V> {
            val baseInstance = headProperty is SelfProperty<*>
            val property : PropertyNullable<D, V> = null!!
//                if (baseInstance) {
//                    originalEntityModel
//                } else {
//                    headProperty.plus(originalEntityModel)
//                }
            return EntityComposedModel(property, originalEntityModel)

        }
    }

    override fun data(): PropertyMetadata<D, V> {
        return composedProperty?.data() ?: throw IllegalArgumentException("invalid model")
    }

    override fun get(entity: D): V? {
        TODO("Not yet implemented")
    }

    override fun set(entity: D, value: V?) {
        TODO("Not yet implemented")
    }
}