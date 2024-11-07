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
package org.ujorm.tools.xml

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.msg.MsgFormatter
import org.ujorm.tools.xml.config.XmlConfig
import java.io.IOException
import java.nio.charset.Charset

/**
 * A generic writer
 * @author Pavel Ponec
 */
abstract class AbstractWriter(
    var writer: Appendable,
    var config: XmlConfig
) {

    /** Value formatter  */
    private val formatter = config.formatter

    /** An indentation request  */
    protected val indentationEnabled: Boolean = Check.hasLength(config.indentation)

    /** Get Writer to escape HTML characters.  */
    val writerEscaped: Appendable = object : Appendable {
        private val attribute = false

        @Throws(IOException::class)
        override fun append(value: CharSequence): Appendable {
            write(value, attribute)
            return this
        }

        @Throws(IOException::class)
        override fun append(value: CharSequence, start: Int, end: Int): Appendable {
            write(value, start, end, attribute)
            return this
        }

        @Throws(IOException::class)
        override fun append(value: Char): Appendable {
            write(value, attribute)
            return this
        }
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    @Throws(IOException::class)
    fun write(text: CharSequence, attribute: Boolean) {
        write(text, 0, text.length, attribute)
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    @Throws(IOException::class)
    fun write(text: CharSequence, from: Int, max: Int, attribute: Boolean) {
        for (i in from until max) {
            write(text[i], attribute)
        }
    }

    /**
     * Write single character to the output
     * @param c Character
     * @param attribute Is it a text to attribute?
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun write(c: Char, attribute: Boolean) {
        when (c) {
            XML_LT -> writer.append(XML_AMPERSAND).append("lt;")
            XML_GT -> writer.append(XML_AMPERSAND).append("gt;")
            XML_AMPERSAND -> writer.append(XML_AMPERSAND).append("amp;")
            XML_2QUOT -> if (attribute) {
                writer.append(XML_AMPERSAND).append("quot;")
            } else {
                writer.append(c)
            }

            XML_APOSTROPHE -> if (true) {
                writer.append(c)
            } else {
                writer.append(XML_AMPERSAND).append("apos;")
            }

            SPACE -> writer.append(c)
            NBSP -> writer.append(XML_AMPERSAND).append("#160;")
            else -> {
                if (c.code > 32) {
                    writer.append(c)
                } else {
                    writer.append(XML_AMPERSAND).append("#")
                    writer.append(c.toString())
                    writer.append(";")
                }
            }
        }
    }

    /** Write escaped value to the output
     * @param value A value to write, where the `null` value is ignored silently.
     * @param element The element
     * @param attributeName A name of the XML attribute of `null` value for a XML text.
     */
    @Throws(IOException::class)
    fun writeValue(
        value: Any?,
        element: ApiElement<*>,
        attributeName: String?
    ) {
        write(formatter.format(value, element, attributeName), attributeName != null)
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    @Throws(IOException::class)
    fun writeRawValue(rawValue: CharSequence, element: ApiElement<*>) {
        writer.append(rawValue)
    }

    /** Write a new line with an offset by the current level  */
    @Throws(IOException::class)
    fun writeNewLine(level: Int) {
        writer.append(config.newLine)
        if (indentationEnabled) {
            for (i in level downTo 1) {
                writer.append(config.indentation)
            }
        }
    }

    override fun toString(): String {
        return writer.toString()
    }

    companion object {
        /** Default XML declaration  */
        const val XML_HEADER: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

        /** Default DOCTYPE of HTML-5  */
        const val HTML_DOCTYPE: String = "<!DOCTYPE html>"

        /** A special XML character  */
        const val XML_GT: Char = '>'

        /** A special XML character  */
        const val XML_LT: Char = '<'

        /** A special XML character  */
        const val XML_AMPERSAND: Char = '&'

        /** A special XML character  */
        const val XML_APOSTROPHE: Char = '\''

        /** A special XML character  */
        const val XML_2QUOT: Char = '"'

        /** A special XML character  */
        const val SPACE: Char = ' '

        /** Non-breaking space character  */
        const val NBSP: Char = '\u00A0'

        /** A forward slash character  */
        const val FORWARD_SLASH: Char = '/'

        /** A CDATA beg markup sequence  */
        const val CDATA_BEG: String = "<![CDATA["

        /** A CDATA end markup sequence  */
        const val CDATA_END: String = "]]>"

        /** A comment beg sequence  */
        const val COMMENT_BEG: String = "<!--"

        /** A comment end sequence  */
        const val COMMENT_END: String = "-->"

        /** Common formatter  */
        val FORMATTER: MsgFormatter = object : MsgFormatter() {}

        // ---- STATIC METHOD(s) ---
        /** Assign a no-cache and an Edge compatibility mode and returns a writer from HttpServletResponse  */
        @Throws(ReflectiveOperationException::class)
        fun createWriter(
            httpServletResponse: Any,
            charset: Charset,
            noCache: Boolean
        ): Appendable {
            val setEncoding = httpServletResponse.javaClass.getMethod(
                "setCharacterEncoding",
                String::class.java
            )
            val setHeader = httpServletResponse.javaClass.getMethod(
                "setHeader",
                String::class.java,
                String::class.java
            )
            val getWriter = httpServletResponse.javaClass.getMethod("getWriter")
            setEncoding.invoke(httpServletResponse, charset.toString())
            setHeader.invoke(httpServletResponse, "Content-Type", "text/html; charset=$charset")
            if (noCache) {
                setHeader.invoke(
                    httpServletResponse,
                    "Cache-Control",
                    "no-cache, no-store, must-revalidate"
                ) // HTTP 1.1
                setHeader.invoke(httpServletResponse, "Pragma", "no-cache") // HTTP 1.0
                setHeader.invoke(httpServletResponse, "Expires", "0") // Proxies
                setHeader.invoke(httpServletResponse, "X-UA-Compatible", "IE=edge") // Proxies
            }
            val writer = getWriter.invoke(httpServletResponse) as Appendable
            return writer
        } //    IT IS A WRONG IDEA:
        //    /** Close the an internal writer, if the one is Closeable */
        //    @Override
        //    public void close() throws IOException {
        //        if (this.out instanceof Closeable) {
        //            ((Closeable) out).close();
        //        }
        //    }
    }
}
