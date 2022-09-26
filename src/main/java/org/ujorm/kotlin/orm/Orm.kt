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
package org.ujorm.kotlin.orm

import org.ujorm.kotlin.core.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*
import java.util.stream.Stream

abstract class AbstractDatabase : AbstractModelProvider() {

    fun <D : Any> select(table: EntityModel<D>): Query<D> {
        TODO()
    }

    fun <D : Any> select(vararg properies: PropertyNullable<D, *>): Query<D> {
        TODO()
    }

    fun <D : Any> where(criterion: Criterion<D, *, *>): Query<D> {
        TODO("Not yet implemented")
    }

    fun save(vararg entities : Entity<*>) {
        TODO()
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

/** Database session */
interface Session {

}