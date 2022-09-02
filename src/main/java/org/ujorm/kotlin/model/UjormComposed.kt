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
package org.ujorm.kotlin.model

import org.ujorm.kotlin.*
import kotlin.reflect.KClass

/** Entity composed model */
abstract class EntityComposedModel<D : Any, V : Any> : PropertyImpl<D, V> {
    /** A name */
    val propertyPrefix: Property<D, *>?

    constructor(
        index: UByte,
        name: String,
        getter: (D) -> V,
        setter: (D, V?) -> Unit,
        entityClass: KClass<D>,
        valueClass: KClass<V>,
        readOnly: Boolean,
        nullable: Boolean,
        propertyPrefix: Property<D, *>?
    ) : super(index, name, getter, setter, entityClass, valueClass, readOnly, nullable) {
        this.propertyPrefix = propertyPrefix
    }
}

class ComposedPropertyMetadata<D : Any, M : Any, V : Any>(
    val primaryProperty: PropertyNullable<D, M>,
    val secondaryProperty: PropertyNullable<M, V>
) : PropertyMetadata<D, V> {

    override val index: UByte get() = primaryProperty.data().index
    override val name: String get() = "${primaryProperty.data().name}.${secondaryProperty.data().name}"
    override val entityClass: KClass<D> get() = primaryProperty.data().entityClass
    override val valueClass: KClass<V> get() = secondaryProperty.data().valueClass
    override val readOnly = primaryProperty.data().readOnly || secondaryProperty.data().readOnly
    override val nullable = primaryProperty.data().nullable || secondaryProperty.data().nullable
}

class ComposedProperty<D : Any, M : Any, V : Any>(
    private val metaData: ComposedPropertyMetadata<D, M, V>
    ) : PropertyNullable<D, V> {

    override fun data() = this.metaData

    override fun get(entity: D): V? {
        val entity2 = metaData.primaryProperty[entity]
        return if (entity2 != null) metaData.secondaryProperty[entity2] else null;
    }

    override fun set(entity: D, value: V?) {
        val entity2 = metaData.primaryProperty[entity]
            ?: throw IllegalArgumentException("Value of property {$metaData.primaryProperty} is null")
        metaData.secondaryProperty.set(entity2, value)
    }
}
