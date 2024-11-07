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

import org.ujorm.tools.web.HtmlElement
import org.ujorm.tools.web.ao.ObjectProvider
import org.ujorm.tools.web.request.RContext
import org.ujorm.tools.xml.config.HtmlConfig
import java.io.Closeable
import java.io.IOException

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
class JsonBuilder protected constructor(
    /** An original writer  */
    private val writer: Appendable,
    /** HTML config  */
    private val config: HtmlConfig = HtmlConfig.ofEmptyElement()
) :
    Closeable {
    /** JSON writer with character escaping  */
    private val jsonWriter = JsonWriter(writer)

    /** Parameter counter  */
    private var paramCounter = 0

    /** Dummy selector to run a JavaScript  */
    private val JAVACRIPT_DUMMY_SELECTOR = ""

    /** Common constructor  */

    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeId(
        elementId: CharSequence,
        vararg values: CharSequence?
    ): JsonBuilder {
        writePrefix(SelectorType.ID.prefix, elementId, *values)
        return this
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeClass(
        elementId: CharSequence,
        vararg values: CharSequence?
    ): JsonBuilder {
        writePrefix(SelectorType.CLASS.prefix, elementId, *values)
        return this
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun write(
        key: CharSequence,
        vararg values: CharSequence?
    ): JsonBuilder {
        writePrefix(SelectorType.INCLUDED.prefix, key, *values)
        return this
    }

    /** Write a key-value with a prefix
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writePrefix(
        keyPrefix: String,
        key: CharSequence,
        vararg values: CharSequence?
    ): JsonBuilder {
        writeKey(keyPrefix, key)
        if (values == null) {
            writer.append("null")
        } else {
            writer.append(DOUBLE_QUOTE)
            for (value in values) {
                jsonWriter.append(value)
            }
        }
        writer.append(DOUBLE_QUOTE)
        return this
    }

    /** Write a Javascript to a call.
     * The response can contain only one Javascript code,
     * so this method can be called only once per request.
     */
    @Throws(IOException::class)
    fun writeJs(vararg javascript: CharSequence?): JsonBuilder {
        return write(JAVACRIPT_DUMMY_SELECTOR, *javascript)
    }

    /** Write a JSON property  */
    @Throws(IOException::class)
    private fun writeKey(keyPrefix: String, key: CharSequence) {
        writer.append(if (paramCounter++ == 0) '{' else ',')
        writer.append(DOUBLE_QUOTE)
        jsonWriter.append(keyPrefix)
        jsonWriter.append(key)
        writer.append(DOUBLE_QUOTE)
        writer.append(':')
    }


    // --- VALUE PROVIDER ---
    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeId(
        elementId: CharSequence,
        valueProvider: ValueProvider
    ): JsonBuilder {
        return write(SelectorType.ID.prefix, elementId, valueProvider)
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeClass(
        elementId: CharSequence,
        valueProvider: ValueProvider
    ): JsonBuilder {
        return write(SelectorType.CLASS.prefix, elementId, valueProvider)
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param valueProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun write(
        key: CharSequence,
        valueProvider: ValueProvider
    ): JsonBuilder {
        return write(SelectorType.INCLUDED.prefix, key, valueProvider)
    }

    /**
     *
     * @param keyPrefix Key Prefix
     * @param key Main Key
     * @param valueProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun write(
        keyPrefix: String,
        key: CharSequence,
        valueProvider: ValueProvider
    ): JsonBuilder {
        writeKey(keyPrefix, key)
        writer.append(DOUBLE_QUOTE)
        HtmlElement.Companion.of(jsonWriter, config).use { root ->
            valueProvider.accept(root.original())
        }
        writer.append(DOUBLE_QUOTE)
        return this
    }

    @Throws(IOException::class)
    override fun close() {
        if (paramCounter == 0) {
            writer.append('{')
        }
        writer.append('}')
    }

    // --- OBJECT PROVIER ---
    /** An experimental feature: write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param objectProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeIdObj(
        elementId: CharSequence,
        objectProvider: ObjectProvider
    ): JsonBuilder {
        return writeObj(SelectorType.ID.prefix, elementId, objectProvider)
    }


    /** An experimental feature: write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param objectProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeClassObj(
        elementId: CharSequence,
        objectProvider: ObjectProvider
    ): JsonBuilder {
        return writeObj(SelectorType.CLASS.prefix, elementId, objectProvider)
    }

    /** An experimental feature: write a key-object value
     *
     * @param key A JSON key
     * @param objectProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeObj(
        key: CharSequence,
        objectProvider: ObjectProvider
    ): JsonBuilder {
        return writeObj(SelectorType.INCLUDED.prefix, key, objectProvider)
    }

    /**
     * An experimental feature: write key-object value
     *
     * @param keyPrefix Key Prefix
     * @param key Main Key
     * @param objectProvider A value provider
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeObj(
        keyPrefix: String,
        key: CharSequence,
        objectProvider: ObjectProvider
    ): JsonBuilder {
        writeKey(keyPrefix, key)
        objectProvider.accept(this)
        writer.append(DOUBLE_QUOTE)
        return this
    }

    /** CSS selector types  */
    enum class SelectorType(val prefix: String) {
        /** CSS selector by ID  */
        ID("#"),

        /** CSS selector by CLASS  */
        CLASS("."),

        /** CSS selector is included  */
        INCLUDED("")
    }

    companion object {
        private val DOUBLE_QUOTE: Char = JsonWriter.Companion.DOUBLE_QUOTE

        // --- UTILS ---
        /** An object factory  */
        fun of(
            context: RContext,
            config: HtmlConfig
        ): JsonBuilder {
            return of(context.writer(), config)
        }

        /** An object factory  */
        fun of(context: RContext): JsonBuilder {
            return of(context.writer())
        }

        /** An object factory  */
        fun of(writer: Appendable): JsonBuilder {
            return JsonBuilder(writer)
        }

        /** An object factory. The MAIN factory method.  */
        fun of(
            writer: Appendable,
            config: HtmlConfig
        ): JsonBuilder {
            return JsonBuilder(writer, config)
        }
    }
}
