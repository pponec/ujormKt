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
package org.ujorm.kotlin.proxy

import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object ProxyUtils {

    fun <E : Any> create(
        entityClass: KClass<E>,
        parent: EntityImplementation? = null,
    ): E {
        if (!entityClass.java.isInterface) {
            throw IllegalArgumentException("An entity class must be defined as an interface.")
        }

        val handler = EntityImplementation(entityClass)
        return Proxy.newProxyInstance(entityClass.java.classLoader, arrayOf(entityClass.java), handler) as E
    }

}