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

import org.ujorm.kotlin.core.impl.RawEntity

interface AbstractEntity<D : Any> {

    /** Provides a RawEntity object.
     * The method name must not match the name of any method of the real entity.
     */
    // @Suppress("INAPPLICABLE_JVM_NAME")
    // @JvmName("___$") // For Java compatibility (?)
    fun `~~`(): RawEntity<D>
}

/**
 * Use this interface if you want to access the entity object via the entity model.
 *
 * WARNING: for better performance of reading and writing values, use the following methods rather:
 * 1. [PropertyNullable.get]
 * 2. [PropertyNullable.set]
 */
interface PropertyAccessor<D : Any> : AbstractEntity<D> {

    /** Method for reading value by property */
    operator fun <V : Any> get(property : PropertyNullable<D, V>) = `~~`().get(property)

    /** Method for reading value by property */
    operator fun <V : Any> get(property : Property<D, V>) = `~~`().get(property) as V

    /** Method for reading value by property */
    operator fun <V : Any> set(property : PropertyNullable<D, V>, value: V?) = `~~`().set(property, value)

    /** Method for reading value by property */
    operator fun <V : Any> set(property : Property<D, V>, value: V) = `~~`().set(property, value)
}


/** A session context */
interface Session {
}

/** Common database recored entity */
interface DbRecord : AbstractEntity<Any>
