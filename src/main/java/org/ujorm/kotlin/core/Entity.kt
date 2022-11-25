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
     * TODO: change this method to the property (?) */
    // @Suppress("INAPPLICABLE_JVM_NAME")
    // @JvmName("___$") // For Java compatibility (?)
    val `~~`: RawEntity<D>
}

/** Use this interface if you want to access the entity object via the entity model. */
interface PropertyAccessor<D : Any> : AbstractEntity<D> {

    /** Method for reading value by property */
    operator fun <V : Any> get(property : PropertyNullable<D, V>) = `~~`[property]

    /** Method for reading value by property */
    operator fun <V : Any> get(property : Property<D, V>) = `~~`[property] as V

    /** Method for reading value by property */
    operator fun <V : Any> set(property : PropertyNullable<D, V>, value: V?) = `~~`.set(property, value)

    /** Method for reading value by property */
    operator fun <V : Any> set(property : Property<D, V>, value: V) = `~~`.set(property, value)
}


/** A session context */
interface Session {
}

/** Common database recored entity */
interface DbRecord : AbstractEntity<Any>
