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
package org.ujorm.tools.xml.config

import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.nio.charset.Charset

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
interface XmlConfig {
    /** Doctype  */
    @JvmField
    val doctype: CharSequence

    /**
     * Charset
     * @return the charset
     */
    @JvmField
    val charset: Charset

    /**
     * Level of the root element, the value may be negative.
     * @return the firstLevel
     */
    @JvmField
    val firstLevel: Int

    /**
     * New line
     * @return the newLine
     */
    @JvmField
    val indentation: CharSequence

    /** A replacement text instead of the `null` value  */
    val defaultValue: CharSequence

    /** A new line sequence  */
    @JvmField
    val newLine: CharSequence

    /**
     * HTTP cache is allowed
     * @return
     */
    val isCacheAllowed: Boolean

    /**
     * Get a value formatter
     */
    val formatter: Formatter

    /** The pair element for termination is required.  */
    fun pairElement(element: ApiElement<*>): Boolean {
        return false
    }

    companion object {
        /**
         * Create a new default config
         * @return
         */
        fun ofDefault(): DefaultXmlConfig {
            return DefaultXmlConfig()
        }

        /**
         * Create a new default config
         * @return
         */
        fun ofDoctype(doctype: String?): DefaultXmlConfig {
            val result = DefaultXmlConfig()
            result.setDoctype(doctype)
            return result
        }
    }
}
