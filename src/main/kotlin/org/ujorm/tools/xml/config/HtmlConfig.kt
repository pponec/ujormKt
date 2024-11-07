/*
 * Copyright 2018-2012 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.util.*

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
interface HtmlConfig : XmlConfig {
    /** Title is a required element by HTML 5  */
    @JvmField
    val title: CharSequence

    /** CSS links of a HTML page  */
    @JvmField
    val cssLinks: Array<CharSequence?>

    /** Language of a HTML page  */
    @JvmField
    val language: Optional<CharSequence>

    /** Get a content type where a recommended value is `"text/html"`  */
    val contentType: String

    /** Build a real model or a plain writer with a recommended value `false`  */
    @JvmField
    val isDocumentObjectModel: Boolean

    /** A request to generate a minimal HTML header  */
    @JvmField
    val isHtmlHeaderRequest: Boolean

    @JvmField
    @get:Deprecated("Use method {@link #getHeaderInjector() }")
    val rawHeaderText: CharSequence?

    @JvmField
    val headerInjector: ApiInjector

    /** A name of root element  */
    @JvmField
    val rootElementName: String

    val unpairElements: Set<String>

    override fun pairElement(element: ApiElement<*>): Boolean {
        return !unpairElements.contains(element.name)
    }

    /** Clone the config for an AJAX processing  */
    fun cloneForAjax(): DefaultHtmlConfig {
        val result = DefaultHtmlConfig(this)
        result.setRootElementName(null)
        result.setNiceFormat<DefaultXmlConfig>()
        result.setDoctype(DefaultXmlConfig.Companion.EMPTY)
        result.setHtmlHeader(false)
        result.setCacheAllowed(false)
        return result
    }

    companion object {
        /**
         * Create a new default config
         */
        @JvmStatic
        fun ofDefault(): DefaultHtmlConfig {
            return DefaultHtmlConfig()
        }

        /**
         * No HTML header is generated, no Doctype and no new lines
         *
         * @param rootElementName Element name cannot contain special HTML characters. An undefined value ignores the creation of the root element.
         * @return
         */
        fun ofElementName(rootElementName: String?): DefaultHtmlConfig {
            return ofElement(rootElementName, true)
        }

        /**
         * No HTML header is generated, no Doctype and no new lines
         *
         * @param rootElementName Element name cannot contain special HTML characters.
         * @param enabled Disabled root element ignores the creation of the root element.
         * @return
         */
        fun ofElement(rootElementName: String?, enabled: Boolean): DefaultHtmlConfig {
            val result = ofDefault()
            result.setRootElementName(if (enabled) rootElementName else null)
            result.setHtmlHeader(false)
            result.setDoctype(DefaultXmlConfig.Companion.EMPTY)
            return result
        }

        /**
         * Create a configuration for an AJAX response.
         */
        @JvmStatic
        fun ofEmptyElement(): DefaultHtmlConfig {
            val result = ofElement(DefaultXmlConfig.Companion.EMPTY, false)
            result.setHtmlHeader(false)
            result.setDoctype(DefaultXmlConfig.Companion.EMPTY)
            result.setNewLine(DefaultXmlConfig.Companion.EMPTY)
            return result
        }

        /** Clone config form another  */
        fun of(htmlConfig: HtmlConfig): DefaultHtmlConfig {
            return DefaultHtmlConfig(htmlConfig)
        }

        /**
         * Create a new configuration with a nice format by an HTML title.
         * @param title If the title is null then create an EMPTY element.
         */
        fun ofTitle(title: String): DefaultHtmlConfig? {
            return ofDefault()
                .setTitle(title)
                .setNiceFormat()
        }
    }
}
