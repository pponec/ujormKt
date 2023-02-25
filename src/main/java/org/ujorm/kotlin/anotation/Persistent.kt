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

package org.ujorm.kotlin.anotation

import org.ujorm.kotlin.core.CriterionProvider
import org.ujorm.kotlin.core.EntityInitializer
import org.ujorm.kotlin.orm.AbstractDatabase
import kotlin.reflect.KClass

/**
 * Optional annotation to indicate a persistent entity.
 *
 * <h4>See more JPA anotations:</h4>
 *
 * https://thorben-janssen.com/key-jpa-hibernate-annotations/
 * https://docs.oracle.com/javaee/7/api/javax/persistence/Entity.html
 * https://docs.oracle.com/javaee/7/api/javax/persistence/package-frame.html
 **/
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Entity(
    /** Entity name */
    val name: String = "",
    /** Entity description */
    val description: String = "",
    /** An implementation of initializing a new entity. */
    val init: KClass<out EntityInitializer<*>> = EntityInitializer::class
)

/** Database table description */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
    val name: String = "",
    val schema: String = "",
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class OneToMany(
    /** An implementation of initializing a new entity. */
    val criterion: KClass<out CriterionProvider<out Any, out AbstractDatabase>> = CriterionProvider::class
)
