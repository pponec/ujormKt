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
package org.ujorm.tools.xml

import java.io.Closeable

/**
 * An element model API.
 *
 * The XmlElement class implements the [Closeable] implementation
 * for an optional highlighting the tree structure in the source code.
 *
 * @since 1.86
 * @author Pavel Ponec
 */
interface ApiElement<E : ApiElement<*>?> : Closeable {
    /** Get an element name  */
    @JvmField
    val name: String

    /** Create a new [ApiElement] for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    fun addElement(name: String): E

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The `null` value is silently ignored. Formatting is performed by the
     * [org.ujorm.tools.xml.model.XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    fun setAttribute(name: String, value: Any?): E

    @Deprecated("Call a method {@link #setAttribute(java.lang.String, java.lang.Object) } rather.")
    fun setAttrib(name: String, value: Any?): E {
        return setAttribute(name, value)
    }

    /**
     * Add a text and escape special character
     * @param value The `null` value is allowed. Formatting is performed by the
     * [org.ujorm.tools.xml.model.XmlWriter.writeValue]  }
     * method, where the default implementation calls a `toString()` only.
     * @return This instance
     */
    fun addText(value: Any?): E

    /**
     * Message template
     *
     * @param template Message template where parameters are marked by the `{}` symbol
     * @param values argument values
     * @return The original builder
     */
    fun addTextTemplated(template: CharSequence?, vararg values: Any): E

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The `null` value is ignored.
     * @return This instance
     */
    fun addRawText(value: Any?): E

    /**
     * Add a **comment text**.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string `-->` .
     * @return This instance
     */
    fun addComment(comment: CharSequence?): E

    /**
     * Add a **character data** in `CDATA` format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    fun addCDATA(charData: CharSequence?): E

    /** Close the element  */
    override fun close()
}