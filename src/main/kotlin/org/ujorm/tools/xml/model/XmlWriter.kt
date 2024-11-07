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

import org.ujorm.tools.Check
import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.builder.XmlBuilder
import org.ujorm.tools.xml.config.XmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.io.IOException

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlModel
 *
 * @since 1.88
 * @author Pavel Ponec
 */
class XmlWriter : AbstractWriter {
    /** Writer constructor with a zero offset  */
    /** Default constructor a zero offset  */
    @JvmOverloads
    constructor(out: Appendable = StringBuilder(512)) : super(out, XmlConfig.Companion.ofDefault())

    /**
     * A writer constructor
     * @param out A writer
     * @param indentationSpace String for a one level offset.
     */
    constructor(out: Appendable, indentationSpace: CharSequence) : super(out, createConfig(indentationSpace))

    /** Render the XML code without header  */
    @Throws(IOException::class)
    fun write(level: Int, element: XmlModel): XmlWriter {
        return write(level, element.getName(), element.attributes, element.children, element)
    }

    /** Render the XML code without header
     * @param level Element nesting level.
     * @param name Name of element where the `null` is allowed for an AJAX responses
     * @param attributes Attributes of the element
     * @param children Childern of the element including `null` items
     * @param element Original element
     * @return This
     */
    @Throws(IOException::class)
    protected fun write(
        level: Int,
        name: CharSequence?,
        attributes: Map<String?, Any?>?,
        children: List<Any?>?,
        element: XmlModel
    ): XmlWriter {
        val validName = name !== XmlBuilder.Companion.HIDDEN_NAME
        if (validName) {
            out.append(AbstractWriter.Companion.XML_LT)
            out.append(name)

            if (Check.hasLength(attributes)) {
                checkNotNull(attributes) // For static analyzer only
                for (key in attributes.keys) {
                    out.append(AbstractWriter.Companion.SPACE)
                    out.append(key)
                    out.append('=')
                    out.append(AbstractWriter.Companion.XML_2QUOT)
                    writeValue(attributes[key], element, key)
                    out.append(AbstractWriter.Companion.XML_2QUOT)
                }
            }
        }
        if (Check.hasLength(children)) {
            checkNotNull(children) // For static analyzer only
            if (validName) {
                out.append(AbstractWriter.Companion.XML_GT)
            }
            var writeNewLine = validName
            for (child in children) {
                if (child is XmlModel) {
                    val xmlChild = child
                    if (writeNewLine && xmlChild.name !== XmlBuilder.Companion.HIDDEN_NAME) {
                        writeNewLine(level)
                    } else {
                        writeNewLine = validName
                    }
                    write(level + 1, xmlChild)
                } else if (child is XmlModel.RawEnvelope) {
                    writeRawValue(child.get().toString(), element)
                    writeNewLine = false
                } else {
                    writeValue(child, element, null)
                    writeNewLine = false
                }
            }
            if (indentationEnabled && writeNewLine && level >= 0) {
                writeNewLine(level - 1)
            }
            if (validName) {
                out.append(AbstractWriter.Companion.XML_LT)
                out.append(AbstractWriter.Companion.FORWARD_SLASH)
                out.append(name)
            }
        } else if (validName) {
            out.append(AbstractWriter.Companion.FORWARD_SLASH)
        }
        if (validName) {
            out.append(AbstractWriter.Companion.XML_GT)
        }
        return this
    }

    companion object {
        /** Create a config  */
        private fun createConfig(indentationSpace: CharSequence): XmlConfig {
            val config: DefaultXmlConfig = XmlConfig.Companion.ofDefault()
            config.setIndentationSpace(indentationSpace)
            return config
        }
    }
}

