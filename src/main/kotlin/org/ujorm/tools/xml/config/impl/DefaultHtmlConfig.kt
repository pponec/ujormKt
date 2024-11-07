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
import org.ujorm.tools.web.Html
import org.ujorm.tools.xml.AbstractWriter
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.builder.XmlBuilder
import org.ujorm.tools.xml.config.ApiInjector
import org.ujorm.tools.xml.config.HtmlConfig
import java.util.*

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
class DefaultHtmlConfig : DefaultXmlConfig, HtmlConfig {
    /** Title  */
    override var title: CharSequence = "Demo"
        private set

    /** Css links with a required order  */
    override var cssLinks: Array<CharSequence?> = arrayOfNulls(0)
        private set

    /** Language of the HTML page  */
    override var language: CharSequence = "en"

    /** Application content type  */
    override var contentType: String = "text/html"
        private set

    /** Build a real model or a plain writer, the default value is `false`  */
    /** Build a real model or a plain writer  */
    override var isDocumentObjectModel: Boolean = false
        private set

    /** A request to generate a minimal HTML header  */
    /** A request to generate a minimal HTML header  */
    override var isHtmlHeaderRequest: Boolean = true
        private set

    /** A raw text for HTML header  */
    /** Raw text to insert to each HTML header  */
    @Deprecated("")
    override var rawHeaderText: CharSequence? = null
        private set

    /** Return a header injector  */
    /** Header injector  */
    override var headerInjector: ApiInjector =
        ApiInjector { e: ApiElement<*>? -> }
        private set

    /** A name of root element  */
    override var rootElementName: String? = XmlBuilder.Companion.HTML

    /** Unpair HTML element names  */
    override var unpairElements: Set<String> = object : HashSet<String?>() {
        init {
            add("area")
            add("base")
            add(Html.BR)
            add("col")
            add("embed")
            add(Html.HR)
            add(Html.IMAGE)
            add(Html.INPUT)
            add("keygen")
            add(Html.LINK)
            add(Html.META)
            add("param")
            // add(Html.SCRIPT); The script is umpair elemnt commonly
            add("source")
            add(Html.STYLE)
            add("track")
        }
    }
        /** Set Unpair element names  */
        set(unpairElements) {
            field = Assert.notNull<Set<String>, String>(
                unpairElements,
                DefaultXmlConfig.Companion.REQUIRED_MSG,
                "unpairElements"
            )
        }

    constructor()

    constructor(htmlConfig: HtmlConfig) : super(htmlConfig) {
        this.title = htmlConfig.title
        this.cssLinks = htmlConfig.cssLinks
        this.language = htmlConfig.language.orElse(null)
        this.contentType = htmlConfig.contentType
        this.isDocumentObjectModel = htmlConfig.isDocumentObjectModel
        this.isHtmlHeaderRequest = htmlConfig.isDocumentObjectModel
        this.rawHeaderText = htmlConfig.rawHeaderText
        this.headerInjector = htmlConfig.headerInjector
        this.rootElementName = htmlConfig.rootElementName
    }

    override fun getDoctype(): CharSequence {
        return nonnull<CharSequence>(doctype, AbstractWriter.Companion.HTML_DOCTYPE)
    }

    override fun getLanguage(): Optional<CharSequence> {
        return Optional.ofNullable(language)
    }

    /** A name of root element  */
    override fun getRootElementName(): String {
        return rootElementName!!
    }

    // --- SETTERS ---
    /** Title is a required element by HTML 5  */
    fun setTitle(title: CharSequence): DefaultHtmlConfig {
        this.title = Assert.notNull(title, "title")
        return this
    }

    fun setCssLinks(vararg cssLinks: CharSequence): DefaultHtmlConfig {
        this.cssLinks =
            Assert.notNull<Array<CharSequence?>, String>(cssLinks, DefaultXmlConfig.Companion.REQUIRED_MSG, "cssLinks")
        return this
    }

    fun setLanguage(language: CharSequence): DefaultHtmlConfig {
        this.language = language
        return this
    }

    fun setContentType(contentType: String): DefaultHtmlConfig {
        this.contentType =
            Assert.notNull<String, String>(contentType, DefaultXmlConfig.Companion.REQUIRED_MSG, "contentType")
        return this
    }

    /** Build a real model or a plain writer, the default value is `false`.
     */
    @Deprecated("Use the method {@link #setDocumentObjectModel(boolean) }.")
    fun setDom(buildDom: Boolean) {
        setDocumentObjectModel(buildDom)
    }

    /** Build a real model or a plain writer, the default value is `false`  */
    fun setDocumentObjectModel(buildDom: Boolean): DefaultHtmlConfig {
        this.isDocumentObjectModel = buildDom
        return this
    }

    /** A request to generate a minimal HTML header  */
    fun setHtmlHeader(htmlHeaderRequest: Boolean): DefaultHtmlConfig {
        this.isHtmlHeaderRequest = htmlHeaderRequest
        return this
    }

    /** The element name must not be special HTML characters.
     * The `null` value is intended to build a root of AJAX queries.
     */
    fun setRootElementName(rootElementName: String?): DefaultHtmlConfig {
        this.rootElementName = rootElementName
            ?: XmlBuilder.Companion.HIDDEN_NAME
        return this
    }

    /**
     * Use the [.setHeaderInjector] method rather.
     * @param rawHeaderText
     * @return
     */
    @Deprecated("")
    fun setRawHedaderCode(rawHeaderText: String?): DefaultHtmlConfig {
        this.rawHeaderText =
            Assert.notNull<String, String>(rawHeaderText, DefaultXmlConfig.Companion.REQUIRED_MSG, "rawHeaderText")
        return this
    }

    /** Assign a new header injector  */
    fun setHeaderInjector(headerInjector: ApiInjector): DefaultHtmlConfig {
        this.headerInjector = Assert.notNull<ApiInjector, String>(
            headerInjector,
            DefaultXmlConfig.Companion.REQUIRED_MSG,
            "headerInjector"
        )
        return this
    }
}
