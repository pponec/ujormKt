/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.web.request.RContext
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.builder.XmlBuilder
import org.ujorm.tools.xml.builder.XmlPrinter
import org.ujorm.tools.xml.config.HtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import org.ujorm.tools.xml.model.XmlModel
import org.ujorm.tools.xml.model.XmlWriter
import java.io.IOException
import java.nio.charset.Charset
import java.util.function.Consumer

/** The root of HTML elements
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 * ServletResponse response = new ServletResponse();
 * try (HtmlElement html = HtmlElement.of(response)) {
 * html.addBody().addHeading("Hello!");
 * }
 * assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
</pre> *
 *
 * For more information see the
 * [next sample](https://jbook-samples-free.ponec.net/sample?src=net.ponec.jbook.s01_hello.HelloWorldElement).
 */
class HtmlElement(
    root: ApiElement<*>,
    /** Config  */
    val config: HtmlConfig,
    /** Config  */
    private val writer: Appendable
) :
    ApiElement<Element?>, Html {
    /** Head element  */
    private val root = Element(root)

    /** Head element  */
    private var head: Element? = null

    /** Body element  */
    var body: Element? = null
        /** Returns a body element  */
        get() {
            if (field == null) {
                field = root.addElement(Html.Companion.BODY)
            }
            return field
        }
        private set

    /** Get config  */

    /** Create new instance with empty html headers  */
    constructor(config: HtmlConfig, writer: Appendable) : this(
        XmlModel(
            Html.Companion.HTML
        ), config, writer
    )

    override val name: String
        get() = root.name

    override fun setAttribute(name: String, value: Any?): Element {
        return root.setAttribute(name, value)
    }

    override fun addText(value: Any?): Element {
        return root.addText(value)
    }

    override fun addTextTemplated(
        template: CharSequence,
        vararg values: Any
    ): Element {
        return root.addTextTemplated(template, *values)
    }

    override fun addRawText(value: Any?): Element {
        return root.addRawText(value)
    }

    override fun addComment(comment: CharSequence?): Element {
        return root.addComment(comment)
    }

    override fun addCDATA(charData: CharSequence?): Element {
        return root.addCDATA(charData)
    }

    /**
     * Create new Element
     * @param name The element name
     * @return New instance of the Element
     * @throws IllegalStateException An envelope for IO exceptions
     */
    @Throws(IllegalStateException::class)
    override fun addElement(name: String): Element {
        return when (name) {
            Html.Companion.HEAD -> getHead()
            Html.Companion.BODY -> body!!
            else -> root.addElement(name)
        }
    }

    /** Returns a head element  */
    fun getHead(): Element {
        if (head == null) {
            head = root.addElement(Html.Companion.HEAD)
        }
        return head!!
    }

    /** Returns a head element  */
    fun addHead(): Element {
        return getHead()
    }

    /** Returns a body element  */
    fun addBody(): Element {
        return body!!
    }

    /** Create a new Javascript element and return it
     * @param javascriptLinks URL list to Javascript
     * @param defer A script that will not run until after the page has loaded
     */
    fun addJavascriptLinks(defer: Boolean, vararg javascriptLinks: CharSequence) {
        for (js in javascriptLinks) {
            addJavascriptLink(defer, js)
        }
    }

    /** Create a new Javascript element and return it
     * @param javascriptLink URL to Javascript
     * @param defer A script that will not run until after the page has loaded
     * @return
     */
    fun addJavascriptLink(defer: Boolean, javascriptLink: CharSequence): Element {
        Assert.notNull(javascriptLink, DefaultXmlConfig.REQUIRED_MSG, "javascriptLink")
        return getHead().addElement(Html.Companion.SCRIPT)
            .setAttribute(Html.Companion.A_SRC, javascriptLink)
            .setAttribute("defer", if (defer) "defer" else null)
    }

    /** Create a new Javascript element and return it.
     * Each item is separated by a new line.
     * @param javascript Add a javascriptLink link
     * @return New CSS element
     */
    fun addJavascriptBody(vararg javascript: CharSequence?): Element {
        if (Check.hasLength<CharSequence>(*javascript)) {
            val result = getHead().addElement(Html.Companion.SCRIPT)
                .setAttribute(Html.Companion.A_LANGUAGE, "javascript")
                .setAttribute(Html.Companion.A_TYPE, "text/javascript")
            var i = 0
            val max = javascript.size
            while (i < max) {
                if (i > 0) {
                    result.addRawText("\n")
                }
                result.addRawText(javascript[i])
                i++
            }
            return result
        }
        return head!!
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     */
    fun addCssLinks(vararg css: CharSequence) {
        for (cssLink in css) {
            addCssLink(cssLink)
        }
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    fun addCssLink(css: CharSequence): Element {
        Assert.notNull(css, DefaultXmlConfig.REQUIRED_MSG, "css")
        return getHead().addElement(Html.Companion.LINK)
            .setAttribute(Html.Companion.A_HREF, css)
            .setAttribute(Html.Companion.A_REL, "stylesheet")
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    fun addCssBody(css: CharSequence): Element {
        Assert.notNull(css, DefaultXmlConfig.REQUIRED_MSG, "css")
        return getHead().addElement(Html.Companion.STYLE)
            .addRawText(css)
    }

    /** Create a new CSS element and return it.
     * Each item is separated by a new line.
     * @param lineSeparator Row separator
     * @param css CSS content rows
     * @return New CSS element
     */
    fun addCssBodies(
        lineSeparator: CharSequence,
        vararg css: CharSequence
    ): Element {
        Assert.hasLength(css, DefaultXmlConfig.REQUIRED_MSG, "css")
        val result = getHead().addElement(Html.Companion.STYLE)
        var i = 0
        val max = css.size
        while (i < max) {
            if (i > 0) {
                result.addRawText(lineSeparator)
            }
            result.addRawText(css[i])

            i++
        }
        return result
    }

    /** Get an original root element  */
    fun original(): Element {
        return root
    }

    /** Returns an Render the HTML code including header. Call the close() method before view  */
    @Throws(IllegalStateException::class)
    override fun toString(): String {
        return writer.toString()
    }

    @Throws(IllegalStateException::class)
    override fun close() {
        root.close()
        if (root.internalElement is XmlModel) {
            try {
                val doctype = config.doctype
                val xmlWriter = XmlWriter(
                    writer
                        .append(doctype)
                        .append(if (doctype.length == 0) "" else config.newLine),
                    config.indentation
                )
                root.internalElement.toWriter(config.firstLevel + 1, xmlWriter)
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }
        }
    }

    val title: CharSequence
        /** Get title of configuration  */
        get() = config.title

    /** Apply body of element by a lambda expression.
     *
     */
    @Deprecated("Use the method {@link #next(Consumer)} rather.")
    fun then(builder: Consumer<HtmlElement?>): ExceptionProvider {
        return next(builder)
    }

    /** Add nested elements to the element.
     *
     * <h3>Usage</h3>
     *
     * <pre class="pre">
     * HtmlElement.of(config, writer).addBody()
     * .next(body -> {
     * body.addHeading(config.getTitle());
     * })
     * .catche(e -> {
     * logger.log(Level.SEVERE, "An error", e);
     * });
    </pre> *
     */
    fun next(builder: Consumer<HtmlElement?>): ExceptionProvider {
        try {
            builder.accept(this)
            return ExceptionProvider.Companion.of()
        } catch (e: RuntimeException) {
            return ExceptionProvider.Companion.of(e)
        } finally {
            close()
        }
    }

    companion object {
        // ------- Static methods ----------
        /** Create root element for a required element name. The MAIN factory method.  */
        @Throws(IllegalStateException::class)
        fun of(
            writer: Appendable,
            myConfig: HtmlConfig
        ): HtmlElement {
            val config = myConfig ?: DefaultHtmlConfig()

            //config.setNiceFormat();
            //config.setCssLinks(cssLinks);
            val root = if (config.isDocumentObjectModel)
                XmlModel(config.rootElementName)
            else
                XmlBuilder(config.rootElementName, XmlPrinter(writer, config), config.firstLevel)
            val result = HtmlElement(root, config, writer)
            if (config.isHtmlHeaderRequest) {
                config.language.ifPresent { lang ->
                    result.setAttribute(
                        Html.Companion.A_LANG,
                        lang
                    )
                }
                result.getHead().addElement(Html.Companion.META).setAttribute(Html.Companion.A_CHARSET, config.charset)
                result.getHead().addElement(Html.Companion.TITLE).addText(config.title)
                result.addCssLinks(config.cssLinks)
                config.headerInjector.write(result.getHead())

                // A deprecated solution:
                val rawHeaderText = config.rawHeaderText
                if (Check.hasLength(rawHeaderText)) {
                    result.getHead().addRawText(config.newLine)
                    result.getHead().addRawText(rawHeaderText)
                }
            }
            return result
        }

        /** Create root element for a required element name. The MAIN factory method.  */
        fun of(
            context: RContext,
            myConfig: HtmlConfig
        ): HtmlElement {
            return of(context.writer(), myConfig)
        }


        /** Create new instance with empty html headers, The MAIN servlet factory method.
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun ofServlet(
            htmlServletResponse: Any,
            config: HtmlConfig?
        ): HtmlElement {
            return of(
                RContext.Companion.ofServlet(null, htmlServletResponse).writer(),
                config!!
            )
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun ofServlet(
            title: String,
            htmlServletResponse: Any,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(RContext.Companion.ofServlet(null, htmlServletResponse).writer(), config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun of(title: CharSequence, response: Appendable, vararg cssLinks: CharSequence): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(response, config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun of(
            title: CharSequence,
            response: Appendable,
            charset: Charset,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(response, config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOf(title: CharSequence, response: Appendable, vararg cssLinks: CharSequence): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(response, config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOf(
            title: CharSequence,
            response: Appendable,
            charset: Charset,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setTitle(title)
            config.setCharset(charset)
            config.setCssLinks(*cssLinks)
            return of(response, config)
        }


        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOfResponse(
            title: String,
            httpServletResponse: Any,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(RContext.Companion.ofServlet(null, httpServletResponse).writer(), config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOfResponse(
            httpServletResponse: Any,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setCssLinks(*cssLinks)
            return of(RContext.Companion.ofServlet(null, httpServletResponse).writer(), config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOf(
            title: String,
            context: RContext,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(context.writer(), config)
        }

        /** Create new instance with empty html headers
         * @throws IllegalStateException IO exceptions
         * @see Appendable
         */
        fun niceOf(
            title: String,
            response: Appendable,
            vararg cssLinks: CharSequence
        ): HtmlElement {
            val config = HtmlConfig.ofDefault()
            config.setNiceFormat<DefaultXmlConfig>()
            config.setTitle(title)
            config.setCssLinks(*cssLinks)
            return of(response, config)
        }

        /** Create new instance with empty html headers
         * @param config Html configuration
         * @return An instance of the HtmlPage
         * @throws IllegalStateException IO exceptions
         */
        @Throws(IllegalStateException::class)
        fun of(config: HtmlConfig?): HtmlElement {
            return of(StringBuilder(256), config!!)
        }
    }
}
