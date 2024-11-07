/*
 * Copyright 2021-2022 Pavel Ponec
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
package org.ujorm.tools.web

import org.ujorm.tools.Assert
import java.util.function.Consumer

/**
 *
 * @author Pavel Ponec
 */
class ExceptionProvider private constructor(private val e: Throwable?) {
    /**
     * Apply consumer if the exception is a required type (or not null).
     */
    fun catchEx(exceptionConsumer: Consumer<Throwable>) {
        catchEx(Throwable::class.java, exceptionConsumer)
    }

    /**
     * Apply consumer if the exception is not null.
     */
    fun <T : Throwable?> catchEx(exceptionClass: Class<T>, exceptionConsumer: Consumer<T>) {
        if (e != null) {
            if (exceptionClass.isInstance(e)) {
                exceptionConsumer.accept(e as T)
            } else if (e is RuntimeException) {
                throw e
            } else if (e is Error) {
                throw e
            } else {
                throw IllegalStateException(e)
            }
        }
    }

    companion object {
        /** Provider with no exception  */
        private val EMPTY = ExceptionProvider(null)

        /** A factory method  */
        fun of(e: Throwable): ExceptionProvider {
            return ExceptionProvider(Assert.notNull(e, "Exception is required"))
        }

        /** A factory method  */
        fun of(): ExceptionProvider {
            return EMPTY
        }
    }
}
