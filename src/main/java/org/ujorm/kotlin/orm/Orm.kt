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
package org.ujorm.kotlin.orm

import org.ujorm.kotlin.core.*
import org.ujorm.kotlin.core.impl.*
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass

abstract class AbstractDatabase : AbstractEntityProvider() {

    fun <D : Any> select(table: EntityModel<D>): Query<D> {
        TODO("Not yet implemented")
    }

    fun <D : Any> select(vararg properies: PropertyNullable<D, *>): Query<D> {
        TODO("Not yet implemented")
    }

    fun <D : Any> where(criterion: Criterion<D, *, *>): Query<D> {
        TODO("Not yet implemented")
    }

    fun save(vararg entities: Any) {
        entities.forEachIndexed { index, element ->
            if (element is AbstractEntity<*>) {
                saveRawEntity(element.`~~`())
            } else {
                val expectedClassName = AbstractEntity::class.simpleName
                throw IllegalArgumentException("The expected type of argument #$index is $expectedClassName.")
            }
        }
    }

    fun saveEntity(vararg entities: AbstractEntity<*>) {
        entities.forEach { saveRawEntity(it.`~~`()) }
    }

    protected fun saveRawEntity(entity: RawEntity<*>) {
        TODO("Not yet implemented")
    }

    // --- Native query

    fun selectToMaps(): NativeQuery {
        TODO("Not yet implemented")
    }

}

open class Query<D : Any> {
    fun where(criterion: Criterion<D, *, *>): Query<D> {
        TODO("Not yet implemented")
    }

    fun limit(limit: Int, offset: Int = 0): Query<D> {
        TODO("Not yet implemented")
    }

    fun orderBy(vararg properies: SortingProperty<D, *>): Query<D> {
        TODO("Not yet implemented")
    }

    /** Sort the result accending */
    fun orderBy(vararg properies: PropertyNullable<D, *>): Query<D> {
        val sortingProperties = properies
            .map { SortingProperty(it, true) }
            .toTypedArray()
        return orderBy(*sortingProperties)
    }

    fun offset(limit: Int): Query<D> {
        TODO("Not yet implemented")
    }

    /** Convert this query to a stream */
    fun toStream(): Stream<D> {
        TODO("Not yet implemented")
    }

    /** Convert this query to a single result */
    fun toList(): List<D> {
        TODO("Not yet implemented")
    }

    /** Convert this query to a single result */
    fun toMaps(): List<PropertyMap> {
        TODO("Not yet implemented")
    }

    /** Convert this query to a single result */
    fun toSingleObject(): D {
        TODO("Not yet implemented")
    }

    /** Convert this query to an optional result */
    fun toOptionalObject(): D? {
        TODO("Not yet implemented")
    }

    fun toCount(): Long {
        TODO("Not yet implemented")
    }
}

open class NativeQuery {
    fun item(vararg expr: Any): Column {
        TODO("Not yet implemented")
        return Column(this);
    }

    @Deprecated("Remove it")
    fun <V : Any> item(map: Pair<PropertyNullable<RawEntity<DbRecord>, V>, Any>, vararg sql: Any): NativeQuery {
        TODO("Not yet implemented")
        return this;
    }

    fun join(vararg expr: CharSequence): Column {
        TODO("Not yet implemented")
        return Column(this);
    }

    fun <V : Any> where(
        c1: PropertyNullable<*, V>,
        operator: CharSequence,
        c2: PropertyNullable<*, V>
    ): NativeQuery {
        TODO("Not yet implemented")
    }

    fun whereAny(vararg expr: Any): NativeQuery {
        TODO("Not yet implemented")
    }

    fun orderBy(vararg expr: CharSequence): NativeQuery {
        TODO("Not yet implemented")
    }

    fun toMaps(): List<PropertyMap> = TODO()
}

open class Column(
    val nativeQuery: NativeQuery
) {
    fun <V : Any> to(property: PropertyNullable<RawEntity<DbRecord>, V>): NativeQuery {
        TODO("Not yet implemented")
        return nativeQuery;
    }
}

/** Database session */
interface Session {

}

/** Property Map */
open class PropertyMap {

    private val map = mutableMapOf<Any, Any?>()

    operator fun <D : Any, V : Any> set(property: PropertyNullable<D, V>, value: V?) {
        map.put(property, value)
    }

    operator fun <D : Any, V : Any> get(key: PropertyNullable<D, V>) = map.get(key) as V?

    operator fun <D : Any, V : Any> set(property: Property<D, V>, value: V) {
        map.put(property, value)
    }

    operator fun <D : Any, V : Any> get(property: Property<D, V>) = map.get(property) as V


    /** Returns optional value */
    fun <D : Any, V : Any> getOptional(property: PropertyNullable<D, V>) =
        Optional.ofNullable(get(property))

}