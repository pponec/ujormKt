/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.json

import org.ujorm.tools.Assert
import java.io.IOException

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
class JsonWriter internal constructor(writer: Appendable) : Appendable {
    private val writer =
        Assert.notNull(writer, "writer")

    @Throws(IOException::class)
    override fun append(csq: CharSequence): Appendable {
        return append(csq, 0, csq.length)
    }

    @Throws(IOException::class)
    override fun append(
        csq: CharSequence,
        start: Int,
        end: Int
    ): Appendable {
        for (i in start until end) {
            append(csq[i])
        }
        return this
    }

    @Throws(IOException::class)
    override fun append(c: Char): Appendable {
        when (c) {
            BACKSLASH -> {
                writer.append(BACKSLASH)
                writer.append(BACKSLASH)
            }

            DOUBLE_QUOTE -> {
                writer.append(BACKSLASH)
                writer.append(DOUBLE_QUOTE)
            }

            '\b' -> {
                writer.append(BACKSLASH)
                writer.append('b')
            }

            '\f' -> {
                writer.append(BACKSLASH)
                writer.append('f')
            }

            '\n' -> {
                writer.append(BACKSLASH)
                writer.append('n')
            }

            '\r' -> {
                writer.append(BACKSLASH)
                writer.append('r')
            }

            '\t' -> {
                writer.append(BACKSLASH)
                writer.append('t')
            }

            else -> writer.append(c)
        }
        return this
    }

    fun original(): Appendable {
        return writer
    }

    companion object {
        private const val BACKSLASH = '\\'
        const val DOUBLE_QUOTE: Char = '"'
    }
}
