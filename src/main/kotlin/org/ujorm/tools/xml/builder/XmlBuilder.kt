/*
 * Copyright 2018-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlElement.java
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

import org.ujorm.tools.Assert
import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.ApiElement
import java.io.Closeable
import java.io.IOException

/**
 * A XML builder.
 * The main benefits are:
 *
 *  * secure building well-formed XML documents  by the Java code
 *  * a simple API built on a single XmlElement class
 *  * creating XML components by a subclass is possible
 *  * great performance and small memory footprint
 *
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 * XmlPriter writer = XmlPriter.forXml();
 * try (XmlBuilder html = new XmlBuilder(Html.HTML, writer)) {
 * try (XmlBuilder head = html.addElement(Html.HEAD)) {
 * head.addElement(Html.META, Html.A_CHARSET, UTF_8);
 * head.addElement(Html.TITLE).addText("Test");
 * }
 * ry (XmlBuilder body = html.addElement(Html.BODY)) {
 * body.addElement(Html.H1).addText("Hello word!");
 * body.addElement(Html.DIV).addText(null);
 * }
 * };
 * String result = writer.toString();
</pre> *
 *
 * The XmlElement class implements the [Closeable] implementation
 * for an optional highlighting the tree structure in the source code.
 * @since 1.86
 * @author Pavel Ponec
 */
class XmlBuilder protected constructor(
    /** Element name  */
    override val name: String,
    writer: XmlPrinter,
    /** Element level  */
    val level: Int,
    printName: Boolean
) : ApiElement<XmlBuilder> {
    /** Writer  */
    /** Node writer  */
    val writer: XmlPrinter

    /** Last child node  */
    private var lastChild: XmlBuilder? = null

    /** The last child was a text  */
    /** The last child was a text  */
    var isLastText: Boolean
        private set

    /** Is Node is filled or it is empty  */
    var isFilled: Boolean = false
        private set

    /** Is the node closed?  */
    /** The node is closed to writing  */
    var isClosed: Boolean = false
        private set

    /** An attribute mode  */
    private var attributeMode = true

    /** The new element constructor
     * @param name The element name must not be special HTML characters.
     * The `null` value is intended to build a root of AJAX queries.
     */
    /** New element with a parent  */
    @JvmOverloads
    constructor(
        name: String,
        writer: XmlPrinter,
        level: Int = 0
    ) : this(name, writer, level, true)

    /** The new element constructor
     * @param name The element name must not be special HTML characters.
     * The `null` value is intended to build a root of AJAX queries.
     * @param writer A XmlPrinter
     * @param level Level of the Element
     * @param printName Print the element name immediately.
     */
    init {
        this.isLastText = name === HIDDEN_NAME
        this.writer = Assert.notNull(writer, REQUIRED_MSG, "writer")

        if (printName) try {
            writer.writeBeg(this, isLastText)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Setup states
     * @param element A child Node or `null` value for a text data
     */
    protected fun nextChild(element: XmlBuilder?): XmlBuilder? {
        Assert.isFalse(isClosed, "The node '{}' was closed", this.name)
        if (!isFilled) try {
            writer.writeMid(this)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        if (lastChild != null) {
            lastChild!!.close()
        }
        if (element != null) try {
            writer.writeBeg(element, isLastText)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }

        isFilled = true
        attributeMode = false
        lastChild = element
        isLastText = element?.name == null

        return element
    }

    /** Create a new [XmlBuilder] for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    override fun addElement(name: String): XmlBuilder {
        val xb = XmlBuilder(name, writer, level + 1, false)
        return nextChild(xb)!!
    }

    /**
     * Add an attribute
     * @param name Required element name
     * @param value The `null` value is ignored. Formatting is performed by the
     * [XmlPrinter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    override fun setAttribute(name: String?, value: Any?): XmlBuilder {
        if (name != null) {
            Assert.hasLength(name, REQUIRED_MSG, "name")
            Assert.isFalse(isClosed, "The node '{}' was closed", name)
            Assert.isTrue(attributeMode, "Writing attributes to the '{}' node was closed", name)
            if (value != null) try {
                writer.writeAttrib(name, value, this)
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }
        }
        return this
    }

    /**
     * Add a text and escape special character
     * @param value The `null` value is allowed. Formatting is performed by the
     * [XmlPrinter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return This instance
     */
    override fun addText(value: Any?): XmlBuilder {
        try {
            nextChild(null)
            writer.writeValue(value, this, null)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        return this
    }

    /**
     * Message template with hight performance.
     *
     * @param template Message template where parameters are marked by the `{}` symbol
     * @param values argument values
     * @return The original builder
     */
    override fun addTextTemplated(template: CharSequence?, vararg values: Any): XmlBuilder {
        try {
            nextChild(null)
            AbstractWriter.Companion.FORMATTER.formatMsg<Any>(writer.writerEscaped, template, *values)
            return this
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The `null` value is ignored.
     * @return This instance
     */
    override fun addRawText(value: Any?): XmlBuilder {
        try {
            nextChild(null)
            writer.writeRawValue(value!!, this)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        return this
    }

    /**
     * Add a **comment text**.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string `-->` .
     * @return This instance
     */
    @Deprecated("")
    override fun addComment(comment: CharSequence?): XmlBuilder {
        throw UnsupportedOperationException()
    }

    /**
     * Add a **character data** in `CDATA` format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Deprecated("")
    override fun addCDATA(charData: CharSequence?): XmlBuilder {
        throw UnsupportedOperationException()
    }

    /** Close the Node  */
    override fun close() {
        if (!isClosed) try {
            isClosed = true
            if (lastChild != null) {
                lastChild!!.close()
            }
            writer.writeEnd(this)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /** Render the XML code including header  */
    override fun toString(): String {
        return writer.toString()
    }

    companion object {
        /** A name of a hidden element must be a unique instance  */
        @JvmField
        var HIDDEN_NAME: String? = ""

        /** The HTML tag name  */
        const val HTML: String = "html"

        /** Assertion message template  */
        protected const val REQUIRED_MSG: String = "The argument '{}' is required"

        // --- Factory method ---
        /** Create builder for HTML  */
        fun forHtml(response: Appendable): XmlBuilder {
            return XmlBuilder(HTML, XmlPrinter.Companion.forHtml(response))
        }

        fun forNiceHtml(response: Appendable): XmlBuilder {
            return XmlBuilder(HTML, XmlPrinter.Companion.forNiceHtml(response))
        }
    }
}