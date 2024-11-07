/*
 * Copyright 2018-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlWriter.java
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
package org.ujorm.tools.xml.builder

import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.config.HtmlConfig
import org.ujorm.tools.xml.config.XmlConfig
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.io.IOException
import java.nio.charset.Charset

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlBuilder
 *
 * @since 1.88
 * @author Pavel Ponec
 */
class XmlPrinter @JvmOverloads constructor(
    out: Appendable = StringBuilder(512),
    config: XmlConfig = XmlConfig.ofDefault()
) :
    AbstractWriter(out, config) {
    /**
     * A writer constructor
     * @param out A writer
     * @param config A configuration object
     */
    /** Writer constructor with a zero offset  */
    /** Default constructor a zero offset  */
    init {
        try {
            writer.append(config.doctype)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    @Throws(IOException::class)
    fun writeRawValue(rawValue: Any, element: XmlBuilder) {
        writer.append(rawValue.toString())
    }

    @Throws(IOException::class)
    fun writeAttrib(name: String, data: Any?, owner: XmlBuilder) {
        if (owner.name !== XmlBuilder.HIDDEN_NAME) {
            writer.append(SPACE)
            writer.append(name)
            writer.append('=')
            writer.append(XML_2QUOT)
            writeValue(data, owner, name)
            writer.append(XML_2QUOT)
        }
    }

    @Throws(IOException::class)
    fun writeRawText(rawText: Any) {
        writer.append(rawText.toString())
    }

    /** Open the Node  */
    @Throws(IOException::class)
    fun writeBeg(element: XmlBuilder, lastText: Boolean) {
        val name: CharSequence = element.name
        if (name !== XmlBuilder.HIDDEN_NAME) {
            if (!lastText) {
                writeNewLine(element.level)
            }
            writer.append()
            writer.append(name)
        }
    }

    /** Middle closing the Node  */
    @Throws(IOException::class)
    fun writeMid(element: XmlBuilder) {
        if (element.name !== XmlBuilder.HIDDEN_NAME) {
            writer.append(XML_GT)
        }
    }

    /** Close the Node  */
    @Throws(IOException::class)
    fun writeEnd(element: XmlBuilder) {
        val name = element.name
        val pairElement = config.pairElement(element)
        val filled = element.isFilled
        if (name !== XmlBuilder.HIDDEN_NAME) {
            if (filled || pairElement) {
                if (indentationEnabled && !element.isLastText) {
                    if (pairElement && !filled) {
                        writer.append(XML_GT)
                    } else {
                        writeNewLine(element.level)
                    }
                } else if (!filled) {
                    writer.append(XML_GT)
                }
                writer.append(XML_LT)
                writer.append(FORWARD_SLASH)
                writer.append(name)
                writer.append(XML_GT)
            } else {
                writer.append(FORWARD_SLASH)
                writer.append(XML_GT)
            }
        }
    }

    override fun toString(): String {
        return writer.toString()
    }

    // ------- FACTORY METHODS -------
    /** Create any element  */
    @Throws(IOException::class)
    fun createElement(name: String): XmlBuilder {
        return XmlBuilder(name, this)
    }

    companion object {
        /** Create a new instance with a formatted output.
         * The result provides a method [.toString]
         * @return New instance of the XmlPrinter
         */
        fun forNiceXml(): XmlPrinter {
            val config: DefaultXmlConfig = XmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            return forXml(null, config)
        }

        /** A basic XmlPrinter factory method.
         * The result provides a method [.toString]
         * @return New instance of the XmlPrinter
         */
        // ------- STATIC METHODS -------
        /** Create a new instance including a XML_HEADER.
         * The result provides a method [.toString]
         */
        @JvmOverloads
        fun forXml(
            out: Appendable? = null,
            config: XmlConfig = XmlConfig.ofDefault()
        ): XmlPrinter {
            return XmlPrinter(out ?: StringBuilder(512), config)
        }

        // --- HTML ---
        /** Create a new instance including a DOCTYPE.
         * The result provides a method [.toString]
         */
        fun forHtml(): XmlPrinter {
            return forXml(null, HtmlConfig.Companion.ofDefault())
        }

        /** Create a new instance including a DOCTYPE  */
        fun forHtml(out: Appendable?): XmlPrinter {
            val config: DefaultHtmlConfig = HtmlConfig.Companion.ofDefault()
            return forXml(out, config)
        }

        /** Create a new instance including a DOCTYPE  */
        fun forNiceHtml(out: Appendable?): XmlPrinter {
            val config: DefaultHtmlConfig = HtmlConfig.Companion.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            return forHtml<Any>(out, config)
        }

        /** Create XmlPrinter for UTF-8  */
        @Throws(IOException::class)
        fun forHtml(httpServletResponse: Any): XmlPrinter {
            val config: DefaultHtmlConfig = HtmlConfig.Companion.ofDefault()
            return forHtml(httpServletResponse, config)
        }

        /** Create XmlPrinter for UTF-8  */
        @Throws(IOException::class)
        fun forNiceHtml(httpServletResponse: Any): XmlPrinter {
            val config: DefaultHtmlConfig = HtmlConfig.Companion.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            return forHtml(httpServletResponse, config)
        }

        /** Create XmlPrinter for UTF-8  */
        private fun <T> forHtml(
            out: Appendable?,
            config: HtmlConfig
        ): XmlPrinter {
            return XmlPrinter(out ?: StringBuilder(512), config)
        }

        /** Create XmlPrinter for UTF-8  */
        @Throws(IOException::class)
        fun forHtml(
            httpServletResponse: Any,
            charset: Charset,
            indentationSpace: String,
            noCache: Boolean
        ): XmlPrinter {
            val config: DefaultHtmlConfig = HtmlConfig.ofDefault()
            config.setCharset(charset)
            config.setIndentationSpace(indentationSpace)
            config.setCacheAllowed(!noCache)
            return forHtml(httpServletResponse, config)
        }

        /** Create XmlPrinter for UTF-8.
         * The basic HTML factory.
         */
        @Throws(IOException::class)
        fun forHtml(
            httpServletResponse: Any,
            config: HtmlConfig
        ): XmlPrinter {
            try {
                val writer: Appendable = createWriter(
                    httpServletResponse,
                    config.charset,
                    config.isCacheAllowed
                )
                return XmlPrinter(writer, config)
            } catch (e: ReflectiveOperationException) {
                throw IllegalArgumentException("Response must be type of HttpServletResponse", e)
            }
        }
    }
}
