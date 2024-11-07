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
package org.ujorm.tools.xml.model

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.config.XmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.io.IOException
import java.io.Serializable
import java.util.*

/**
 * XML element **model** to rendering a XML file.
 * The main benefits are:
 *
 *  * secure building well-formed XML documents  by the Java code
 *  * a simple API built on a single XmlElement class
 *  * creating XML components by a subclass is possible
 *  * great performance and small memory footprint
 * ¨
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 * XmlElement root = new XmlElement("root");
 * root.addElement("childA")
 * .setAttrib("x", 1)
 * .setAttrib("y", 2);
 * root.addElement("childB")
 * .setAttrib("x", 3)
 * .setAttrib("y", 4)
 * .addText("A text message &lt;&\"&gt;");
 * root.addRawText("\n&lt;rawXml/&gt;\n");
 * root.addCDATA("A character data &lt;&\"&gt;");
 * String result = root.toString();
</pre> *
 *
 * @since 2.03
 * @author Pavel Ponec
 */
class XmlModel
/**
 * @param name The element name must not be special HTML characters.
 * The `null` value is intended to build a root of AJAX queries.
 */(
    /** Element name  */
    override val name: String
) : ApiElement<XmlModel>, Serializable {
    /** Attributes  */
    var attributes: MutableMap<String, Any>? = null

    /** Child elements with a `null` items  */
    var children: MutableList<Any?>? = null

    /** New element with a parent  */
    constructor(name: String, parent: XmlModel) : this(name) {
        parent.addChild(this)
    }

    override fun getName(): String? {
        return name
    }

    protected val attribs: Map<String, Any>
        /** Return attributes  */
        get() {
            if (attributes == null) {
                attributes = LinkedHashMap()
            }
            return attributes!!
        }

    /** Add a child entity  */
    protected fun addChild(child: Any?) {
        if (children == null) {
            children = ArrayList()
        }
        children!!.add(child)
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement!
     */
    fun addElement(element: XmlModel): XmlModel {
        addChild(Assert.notNull<XmlModel, String>(element, DefaultXmlConfig.Companion.REQUIRED_MSG, "element"))
        return element
    }

    /** Create a new [XmlModel] for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    override fun addElement(name: String): XmlModel {
        return XmlModel(name, this)
    }

    /**
     * Set one attribute
     * @param name Required element name
     * @param value The `null` value is ignored. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    override fun setAttribute(name: String, value: Any?): XmlModel {
        Assert.hasLength<String, String>(name, DefaultXmlConfig.Companion.REQUIRED_MSG, "name")
        if (value != null) {
            if (attributes == null) {
                attributes = LinkedHashMap()
            }
            attributes!![name] = value
        }
        return this
    }

    /**
     * Add a text and escape special character
     * @param value The `null` value is allowed. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return This instance
     */
    override fun addText(value: Any?): XmlModel {
        addChild(value)
        return this
    }

    /**
     * Message template with hight performance.
     *
     * @param template Message template where parameters are marked by the `{}` symbol
     * @param values argument values
     * @return The original builder
     */
    override fun addTextTemplated(template: CharSequence?, vararg values: Any): XmlModel {
        try {
            return addText(AbstractWriter.Companion.FORMATTER.formatMsg<Any>(null, template, *values))
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }


    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The `null` value is ignored.
     * @return This instance
     */
    override fun addRawText(value: Any?): XmlModel {
        if (value != null) {
            addChild(RawEnvelope(value))
        }
        return this
    }

    /**
     * Add a **comment text**.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string `-->` .
     * @return This instance
     */
    override fun addComment(comment: CharSequence?): XmlModel {
        if (Check.hasLength(comment)) {
            Assert.isTrue<String>(
                !comment.toString().contains(AbstractWriter.Companion.COMMENT_END),
                "The text contains a forbidden string: " + AbstractWriter.Companion.COMMENT_END
            )
            val msg: StringBuilder = StringBuilder(
                (AbstractWriter.Companion.COMMENT_BEG.length
                        + AbstractWriter.Companion.COMMENT_END.length
                        + comment!!.length + 2)
            )
            addRawText(
                msg.append(AbstractWriter.Companion.COMMENT_BEG)
                    .append(AbstractWriter.Companion.SPACE)
                    .append(comment)
                    .append(AbstractWriter.Companion.SPACE)
                    .append(AbstractWriter.Companion.COMMENT_END)
            )
        }
        return this
    }

    /**
     * Add a **character data** in `CDATA` format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    override fun addCDATA(charData: CharSequence?): XmlModel {
        if (Check.hasLength(charData)) {
            addRawText(AbstractWriter.Companion.CDATA_BEG)
            val text = charData.toString()
            var i = 0
            var j: Int
            while ((text.indexOf(AbstractWriter.Companion.CDATA_END, i).also { j = it }) >= 0) {
                j += AbstractWriter.Companion.CDATA_END.length
                addRawText(text.subSequence(i, j))
                i = j
                addText(AbstractWriter.Companion.CDATA_END)
                addRawText(AbstractWriter.Companion.CDATA_BEG)
            }
            addRawText(if (i == 0) text else text.substring(i))
            addRawText(AbstractWriter.Companion.CDATA_END)
        }
        return this
    }

    /** Get an unmodifiable map of attributes  */
    fun getAttributes(): Map<String, Any> {
        return if (attributes != null)
            Collections.unmodifiableMap(attributes)
        else emptyMap()
    }

    /** Get an unmodifiable list of children  */
    fun getChildren(): List<Any?> {
        return if (children != null)
            Collections.unmodifiableList(children)
        else emptyList<Any>()
    }

    /** An empty method  */
    override fun close() {
    }

    /** Render the XML code including header  */
    override fun toString(): String {
        try {
            val config: XmlConfig = XmlConfig.Companion.ofDefault()
            val writer = XmlWriter(
                StringBuilder(512)
                    .append(AbstractWriter.Companion.XML_HEADER)
                    .append(config.newLine)
            )
            return toWriter(0, writer).toString()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /** Render the XML code without header  */
    @Throws(IOException::class)
    fun toWriter(level: Int, out: XmlWriter): XmlWriter {
        return out.write(level, this)
    }

    // -------- Inner class --------
    /** Raw XML code envelope  */
    class RawEnvelope(
        /** XML content  */
        private val body: Any
    ) {
        /** Get the body  */
        fun get(): Any {
            return body
        }
    }
}