/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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
package org.ujorm.tools.xml.config.impl

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.config.Formatter
import org.ujorm.tools.xml.config.XmlConfig
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Configuration of HtmlPage
 * @author Pavel Ponec
 */
open class DefaultXmlConfig : XmlConfig {
    /** A header declaration of the document or a doctype  */
    override var doctype: CharSequence? = null

    /**
     * Charset
     * @return the charset
     */
    /** Charset  */
    override var charset: Charset = StandardCharsets.UTF_8
        private set

    /**
     * Level of the root element, the value may be negative.
     * @return the firstLevel
     */
    /** Level of the root element, the value may be negative number  */
    override var firstLevel: Int = DEFAULT_FIRST_LEVEL
        private set

    /** An indentation space for elements of the next level,
     * where default value is an empty `String`  */
    override var indentation: CharSequence = EMPTY

    /** A replacement text instead of the `null` value  */
    /** A replacement text instead of the `null` value  */
    override var defaultValue: CharSequence = EMPTY
        private set

    /** A new line sequence  */
    /** A new line sequence  */
    override var newLine: CharSequence = DEFAULT_NEW_LINE
        private set

    /** Is HTTP cache allowed  */
    override var isCacheAllowed: Boolean = false
        private set

    /** A default implementation is: `String.valueOf(value)`  */
    /** A value formatter where a default implemnetation is:
     * `
     * `Formatter formatter -> value != null ? value.toString() : ""`;
    ` *
     */
    override var formatter: Formatter =
        Formatter { value: Any?, element: ApiElement<*>?, attribute: String? ->
            value?.toString()
                ?: EMPTY
        }
        private set

    constructor()

    /** Copy attributes from other config  */
    constructor(config: XmlConfig) {
        this.doctype = config.doctype
        this.charset = config.charset
        this.firstLevel = config.firstLevel
        this.indentation = config.indentation
        this.defaultValue = config.defaultValue
        this.newLine = config.newLine
        this.isCacheAllowed = config.isCacheAllowed
        this.formatter = config.formatter
    }

    /** A header declaration of the document or a doctype  */
    override fun getDoctype(): CharSequence {
        return nonnull<CharSequence>(doctype, AbstractWriter.Companion.XML_HEADER)
    }

    protected fun <T> nonnull(value: T?, defaultValue: T): T {
        return value ?: defaultValue
    }

    /** A header declaration of the document or a doctype  */
    fun setDoctype(doctype: CharSequence?): DefaultXmlConfig {
        this.doctype = doctype
        return this
    }

    /**
     * Charset
     * @param charset the charset to set
     */
    fun setCharset(charset: Charset): DefaultXmlConfig {
        this.charset = Assert.notNull(charset, REQUIRED_MSG, "charset")
        return this
    }

    /**
     * Assign parameters for a nice format of the HTML result
     */
    fun <T : DefaultXmlConfig?> setNiceFormat(): T {
        setNiceFormat<DefaultXmlConfig>(DEFAULT_INTENDATION)
        return this as T
    }

    /**
     * Assign parameters for a nice format of the HTML result
     * @param indentation An empty String is replaced by a default intendation.
     */
    fun <T : DefaultXmlConfig?> setNiceFormat(indentation: CharSequence?): T {
        this.firstLevel = 0
        this.indentation = if (Check.hasLength(indentation)) indentation!! else DEFAULT_INTENDATION
        this.newLine = DEFAULT_NEW_LINE
        return this as T
    }

    /**
     * Assign parameters for a compressed format of the HTML result
     */
    fun setCompressedFormat(): DefaultXmlConfig {
        this.firstLevel = DEFAULT_FIRST_LEVEL
        this.indentation = EMPTY
        this.newLine = EMPTY
        return this
    }

    /**
     * Level of the root element, the value may be negative.
     * @param firstLevel the firstLevel to set
     */
    fun setFirstLevel(firstLevel: Int): DefaultXmlConfig {
        this.firstLevel = firstLevel
        return this
    }

    /** An indentation space for elements of the next level,
     * where default value is an empty `String`  */
    override fun getIndentation(): CharSequence {
        return nonnull(indentation, EMPTY)
    }

    /** An indentation space for elements of the next level,
     * where default value is an empty `String`  */
    fun setIndentationSpace(indentation: CharSequence): DefaultXmlConfig {
        this.indentation = Assert.notNull(indentation, REQUIRED_MSG, "indentation")
        return this
    }


    // --- SETTERS ---
    /** A replacement text instead of the `null` value  */
    fun setDefaultValue(defaultValue: String): DefaultXmlConfig {
        this.defaultValue = Assert.notNull(defaultValue, "defaultValue")
        return this
    }

    fun setCacheAllowed(cacheAllowed: Boolean): DefaultXmlConfig {
        this.isCacheAllowed = cacheAllowed
        return this
    }

    /** A new line sequence  */
    fun setNewLine(newLine: CharSequence): DefaultXmlConfig {
        this.newLine = Assert.notNull(newLine, "newLine")
        return this
    }

    /** A default value formatter is implemented by the method `String.valueOf(value)`  */
    fun setFormatter(formatter: Formatter): DefaultXmlConfig {
        this.formatter = Assert.notNull(formatter, "formatter")
        return this
    }

    companion object {
        /** Default intendation per level  */
        const val DEFAULT_INTENDATION: String = "\t"

        /** Default string or the new line  */
        const val DEFAULT_NEW_LINE: String = "\n"

        /** Default first level of intendation  */
        const val DEFAULT_FIRST_LEVEL: Int = Int.MIN_VALUE + 1

        /** Assertion message template  */
        const val REQUIRED_MSG: String = "The argument {} is required"

        /** An empty String  */
        const val EMPTY: String = ""
    }
}
