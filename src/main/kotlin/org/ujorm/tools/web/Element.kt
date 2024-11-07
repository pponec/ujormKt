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
package org.ujorm.tools.web

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.web.ao.Column
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.ao.Injector
import org.ujorm.tools.web.ao.WebUtils
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.builder.XmlBuilder
import org.ujorm.tools.xml.model.XmlModel
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * A HTML Element implements some methods for frequently used elements and attributes
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 * ServletResponse response = new ServletResponse();
 * try (HtmlElement html = HtmlElement.of(response)) {
 * try (Element body = html.getBody()) {
 * body.addHeading("Hello!");
 * }
 * }
 * assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
</pre> *
 *
 * @see HtmlElement.of
 */
class Element
/** New element for an API element
 * @see .of
 */ internal constructor(
    /** An original XML element  */
    val internalElement: ApiElement<*>
) : ApiElement<Element?>,
    Html {
    override val name: String
        get() = internalElement.name

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The `null` value is silently ignored. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    override fun setAttribute(name: String, value: Any?): Element {
        internalElement.setAttribute(name, value)
        return this
    }

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The `null` value is silently ignored. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    fun setAttributes(
        name: String,
        separator: CharSequence,
        vararg value: Any
    ): Element {
        val `val` = Stream.of(*value)
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .map { v: Any -> v.toString() }
            .collect(Collectors.joining(separator))
        internalElement.setAttribute(name, `val`)
        return this
    }

    /**
     * Set an attribute with no value
     * @param name Required element name
     * @return The original element
     */
    fun setAttribute(name: String): Element {
        return setAttribute(name, "")
    }

    /**
     * An deprecated shortcut for the method [.setAttribute].
     * @param name Required element name
     * @param value The `null` value is silently ignored. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    /**
     * A shortcut for the method [.setAttribute].
     * @param name Required element name
     * @param value The `null` value is silently ignored. Formatting is performed by the
     * [XmlWriter.writeValue]
     * method, where the default implementation calls a `toString()` only.
     * @return The original element
     */
    fun setAttr(name: String, value: Any?): Element {
        return setAttribute(name, value)
    }

    /** Add simple text
     * @param data Text item
     * @return A parent element.
     * @see .addAnchoredText
     */
    @Throws(IllegalStateException::class)
    override fun addText(data: Any?): Element {
        internalElement.addText(data)
        return this
    }

    /**
     * Add many texts with **no separator**
     * @param data Text items
     * @return A parent element.
     * @see .addAnchoredText
     */
    @Throws(IllegalStateException::class)
    fun addText(vararg data: Any): Element {
        return addTexts("", *data)
    }

    /**
     * Add a template based text with parameters with hight performance.
     *
     * @param template A message template with an ENGLISH locale. See [for more parameters.][String.format]
     */
    override fun addTextTemplated(template: CharSequence?, vararg values: Any): Element {
        internalElement.addTextTemplated(template, *values)
        return this
    }

    /**
     * Add many words separated by a delimeter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addTexts(
        separator: CharSequence,
        vararg data: Any
    ): Element {
        var i = 0
        val max = data.size
        while (i < max) {
            if (i > 0) {
                internalElement.addRawText(separator)
            }
            internalElement.addText(data[i])
            i++
        }
        return this
    }

    @Throws(IllegalStateException::class)
    override fun addRawText(data: Any?): Element {
        internalElement.addRawText(data)
        return this
    }

    @Throws(IllegalStateException::class)
    fun addRawText(vararg data: Any): Element {
        for (item in data) {
            internalElement.addRawText(item)
        }
        return this
    }

    /**
     * Add many words separated by a delimeter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addRawTexts(
        separator: CharSequence,
        vararg data: Any
    ): Element {
        var i = 0
        val max = data.size
        while (i < max) {
            if (i > 0) {
                internalElement.addRawText(separator)
            }
            internalElement.addRawText(data[i])
            i++
        }
        return this
    }

    @Throws(IllegalStateException::class)
    override fun addComment(comment: CharSequence?): Element {
        internalElement.addComment(comment)
        return this
    }

    @Throws(IllegalStateException::class)
    override fun addCDATA(charData: CharSequence?): Element {
        internalElement.addCDATA(charData)
        return this
    }

    @Throws(IllegalStateException::class)
    override fun close() {
        internalElement.close()
    }

    // -------------- Add ELEMENT -----
    /**
     * Create new Element
     * @param name The element name
     * @return New instance of the Element
     * @throws IllegalStateException An envelope for IO exceptions
     */
    @Throws(IllegalStateException::class)
    override fun addElement(name: String): Element {
        return Element(internalElement.addElement(name)!!)
    }

    /**
     * Add a new Element with optional CSS classes
     * @param name A required name of the element
     * @param cssClasses Optional CSS classes.
     * @return New instance of the Element
     */
    fun addElement(name: String, vararg cssClasses: CharSequence): Element {
        return addElement(name).setClass(*cssClasses)
    }

    /**
     * Add an element according to a condition.
     * @param enabled A condition for rendering the element.
     * @param name An element name
     * @param cssClasses CSS classes
     * @return New instance of the Element
     */
    fun addElementIf(
        enabled: Boolean,
        name: String,
        vararg cssClasses: CharSequence
    ): Element {
        return addElement((if (enabled) name else XmlBuilder.HIDDEN_NAME)!!).setClass(*cssClasses)
    }

    /** Add new Table  */
    fun addTable(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.TABLE, *cssClasses)
    }

    /** Create a HTML table according to data  */
    fun addTable(
        data: Array<Array<Any?>>,
        vararg cssClass: CharSequence
    ): Element {
        return addTable(Arrays.asList<Array<Any>>(*data), *cssClass)
    }

    /** Create a HTML table according to data  */
    fun addTable(
        data: Collection<Array<Any>>,
        vararg cssClass: CharSequence
    ): Element {
        val result = addTable(*cssClass)
        for (rowValue in data) {
            if (rowValue != null) {
                val rowElement = result.addElement(Html.Companion.TR)
                for (value in rowValue) {
                    rowElement.addElement(Html.Companion.TD).addText(value)
                }
            }
        }
        return result
    }

    /** Create a HTML table according to data
     *
     * <h3>Usage</h3>
     * <pre>
     * element.addTable(getCars(), cssClasses, titles,
     * Car::getId,
     * Car::getName,
     * Car::getEnabled);
    </pre> *
     */
    fun <D, V> addTable(
        domains: Stream<D>,
        cssClass: Array<CharSequence?>?,
        headers: Array<Any?>?,
        vararg attributes: Function<D, V>
    ): Element {
        val result: Element = addTable(*cssClass ?: arrayOfNulls<String>(0))
        if (Check.hasLength<Any>(*headers)) {
            val rowElement = result.addElement(Html.Companion.THEAD).addElement(Html.Companion.TR)
            for (value in headers!!) {
                val th = rowElement.addElement(Html.Companion.TH)
                if (value is Injector) {
                    value.write(th)
                } else {
                    th.addText(value)
                }
            }
        }
        result.addElement(Html.Companion.TBODY).use { tBody ->
            val hasRenderer = WebUtils.isType(
                Column::class.java, *attributes
            )
            domains.forEach { value: D ->
                val rowElement = tBody.addElement(Html.Companion.TR)
                for (attribute in attributes) {
                    val td = rowElement.addElement(Html.Companion.TD)
                    if (hasRenderer && attribute is Column<*>) {
                        (attribute as Column<*>).write(td, value)
                    } else {
                        td.addText(attribute.apply(value))
                    }
                }
            }
        }
        return result
    }

    /**
     * Add a link to an image
     * @param imageLink A link to image
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addImage(
        imageLink: CharSequence,
        alt: CharSequence,
        vararg cssClasses: CharSequence
    ): Element {
        return addElement(Html.Companion.IMAGE, *cssClasses)
            .setAttribute(Html.Companion.A_ALT, alt)
            .setAttribute(Html.Companion.A_SRC, imageLink)
    }

    /**
     * Add an embeded image
     * @param imageStream Stream provides a PNG image and it will be closed after reading.
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addImage(
        imageStream: InputStream,
        alt: CharSequence,
        vararg cssClasses: CharSequence
    ): Element {
        return addElement(Html.Companion.IMAGE, *cssClasses)
            .setAttribute(Html.Companion.A_ALT, alt)
            .setAttribute(Html.Companion.A_SRC, createEmbededImage(imageStream, StringBuilder(1024)))
    }

    /** Create a content of an embeded image  */
    private fun createEmbededImage(
        imageStream: InputStream,
        result: StringBuilder
    ): CharSequence {
        val bufferSize = 3 * 1024
        val encoder = Base64.getEncoder()
        try {
            BufferedInputStream(imageStream).use { `in` ->
                result.append("data:image/png;base64,")
                var chunk = ByteArray(bufferSize)
                var len = 0
                while ((`in`.read(chunk).also { len = it }) == bufferSize) {
                    result.append(encoder.encodeToString(chunk))
                }
                if (len > 0) {
                    chunk = chunk.copyOf(len)
                    result.append(encoder.encodeToString(chunk))
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException(e.message, e)
        }
        return result
    }

    /** Add new body element  */
    fun addBody(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.BODY, *cssClasses)
    }

    /** Add new title element  */
    fun addTitle(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.TITLE, *cssClasses)
    }

    /** Add new link element  */
    fun addLink(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.LINK, *cssClasses)
    }

    /** Add new style element  */
    fun addStyle(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.STYLE, *cssClasses)
    }

    /** Add new script element  */
    fun addScript(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.SCRIPT, *cssClasses)
    }

    /** Add new div element  */
    fun addDiv(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.DIV, *cssClasses)
    }

    /** Add new fieldset element including a title
     * @param title An optional title
     * @param cssClasses CSS classes
     * @return An instance of FieldSet
     * @see LEGEND
     */
    fun addFieldset(title: String?, vararg cssClasses: CharSequence): Element {
        val result = addElement(Html.Companion.FIELDSET, *cssClasses)
        if (Check.hasLength(title)) {
            result.addElement(Html.Companion.LEGEND).addText(title)
        }
        return result
    }

    /** Add new pre element  */
    fun addPreformatted(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.PRE, *cssClasses)
    }

    /** Add new span element  */
    fun addSpan(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.SPAN, *cssClasses)
    }

    /** Add new paragram element  */
    fun addParagraph(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.P, *cssClasses)
    }

    /** Add new form element  */
    fun addForm(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.FORM, *cssClasses)
    }

    /** Add a top heading (level one)   */
    fun addHeading(title: CharSequence, vararg cssClasses: CharSequence): Element {
        return addHeading(1, title, *cssClasses)
    }

    /** Add new heading with the required level where the first level is the one,   */
    fun addHeading(level: Int, title: CharSequence, vararg cssClasses: CharSequence): Element {
        Assert.isTrue(level > 0, "Unsupported level {}", level)
        return addElement(Html.Companion.HEADING_PREFIX + level, *cssClasses).addText(title)
    }

    /** Add new head of table element  */
    fun addTableHead(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.THEAD, *cssClasses)
    }

    /** Add new table row element  */
    fun addTableRow(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.TR, *cssClasses)
    }

    /** Add new detail of table element  */
    fun addTableDetail(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.TD, *cssClasses)
    }

    /** Add new label element  */
    fun addLabel(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.LABEL, *cssClasses)
    }

    /** Add new input element  */
    fun addInput(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.INPUT, *cssClasses)
    }

    /** Add new input element type of text  */
    fun addTextInput(vararg cssClasses: CharSequence): Element {
        return addInput(*cssClasses).setType(Html.Companion.V_TEXT)
    }

    /** Add new input element type of text including attributes: name, value, placeholder and title  */
    fun <V> addTextInp(
        param: HttpParameter,
        value: V?,
        title: CharSequence,
        vararg cssClasses: CharSequence
    ): Element {
        return addTextInput(*cssClasses)
            .setName(param)
            .setValue(value)
            .setAttribute(Html.Companion.A_PLACEHOLDER, title)
            .setAttribute(Html.Companion.A_TITLE, title)
    }

    /** Add a new password input element  */
    fun addPasswordInput(vararg cssClasses: CharSequence): Element {
        return addInput(*cssClasses).setType(Html.Companion.V_PASSWORD)
    }

    /** Add a new hidden input element with a name &amp; value  */
    fun addHiddenInput(
        name: CharSequence?,
        value: Any?
    ): Element {
        return addInput().setType(Html.Companion.V_HIDDEN).setNameValue(name, value)
    }

    /** Add new text area element  */
    fun addTextArea(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.TEXT_AREA, *cssClasses)
    }

    /** Add new select element
     * @see .addSelectOptions
     */
    fun addSelect(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.SELECT, *cssClasses)
    }

    /** Add options from map to current select element
     * @param value Value of a select element
     * @param options Consider an instance of the [LinkedHashMap] class predictable iteration order of options.
     * @param cssClasses
     * @return Return `this`
     * @see .addSelect
     */
    fun addSelectOptions(
        value: Any,
        options: Map<*, *>,
        vararg cssClasses: CharSequence
    ): Element {
        for (key in options.keys) {
            addElement(Html.Companion.OPTION)
                .setAttribute(Html.Companion.A_VALUE, key)
                .setAttribute(Html.Companion.A_SELECTED, if (value == key) Html.Companion.A_SELECTED else null)
                .addText(options[key])
        }
        return this
    }

    /** Add new option element  */
    fun addOption(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.OPTION, *cssClasses)
    }

    /** Add new button element  */
    fun addButton(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.BUTTON, *cssClasses)
    }

    /** Add a submit button  */
    fun addSubmitButton(vararg cssClasses: CharSequence): Element {
        val result = addButton(*cssClasses)
        return result.setType(Html.Companion.V_SUBMIT)
    }

    /** Add an anchor element with URL and CSS classes  */
    fun addAnchor(url: String, vararg cssClasses: CharSequence): Element {
        val result = addElement(Html.Companion.A, *cssClasses)
        return result.setHref(url)
    }

    /**
     * Add a
     * @param url
     * @param text
     * @return The original element!
     */
    fun addLinkedText(url: String, vararg text: Any): Element {
        addElement(Html.Companion.A).setHref(url).addTexts("", *text)
        return this
    }

    /** Add new unordered list element  */
    fun addUnorderedlist(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.UL, *cssClasses)
    }

    /** Add new ordered list element  */
    fun addOrderedList(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.OL, *cssClasses)
    }

    fun addListItem(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.LI, *cssClasses)
    }

    /** Set a CSS class attribute optionally, the empty attribute is ignored.
     * @param cssClasses Optional CSS classes. The css item is ignored when the value is empty or `null`.
     * @return The current instanlce
     */
    fun setClass(vararg cssClasses: CharSequence): Element {
        if (Check.hasLength<CharSequence>(*cssClasses)) {
            val builder = StringJoiner(" ")
            for (cssClass in cssClasses) {
                if (Check.hasLength(cssClass)) {
                    builder.add(cssClass)
                }
            }
            val result = builder.toString()
            if (Check.hasLength(result)) {
                setAttribute(Html.Companion.A_CLASS, result)
            }
        }
        return this
    }

    /** Add a line break  */
    fun addBreak(vararg cssClasses: CharSequence): Element {
        return addElement(Html.Companion.BR, *cssClasses)
    }

    /** Set an identifier of the element  */
    fun setId(value: CharSequence?): Element {
        setAttribute(Html.Companion.A_ID, value)
        return this
    }

    /** Set a method of form  */
    fun setMethod(value: Any?): Element {
        setAttribute(Html.Companion.A_METHOD, value)
        return this
    }

    /** Set an action type of from  */
    fun setAction(value: Any?): Element {
        setAttribute(Html.Companion.A_ACTION, value)
        return this
    }

    /** Set a type of input element  */
    fun setType(value: Any?): Element {
        setAttribute(Html.Companion.A_TYPE, value)
        return this
    }

    /** Set an name of input element  */
    fun setName(value: CharSequence?): Element {
        setAttribute(Html.Companion.A_NAME, value)
        return this
    }

    /** Set an value of input element  */
    fun setValue(value: Any?): Element {
        setAttribute(Html.Companion.A_VALUE, value)
        return this
    }

    /** Set name &amp; value to the input element  */
    fun setNameValue(name: CharSequence?, value: Any?): Element {
        return setName(name).setValue(value)
    }

    /** Set an value of input element  */
    fun setFor(value: CharSequence?): Element {
        setAttribute(Html.Companion.A_FOR, value)
        return this
    }

    /** Row count of a text area  */
    fun setRows(value: Int): Element {
        setAttribute(Html.Companion.A_ROWS, value)
        return this
    }

    /** Column count of a text area  */
    fun setCols(value: Any?): Element {
        setAttribute(Html.Companion.A_COLS, value)
        return this
    }

    /** Column span inside the table  */
    fun setColSpan(value: Int): Element {
        setAttribute(Html.Companion.A_COLSPAN, value)
        return this
    }

    /** Row span inside the table  */
    fun setRowSpan(value: Int): Element {
        setAttribute(Html.Companion.A_ROWSPAN, value)
        return this
    }

    /** Set hyperlink reference  */
    fun setHref(value: CharSequence?): Element {
        setAttribute(Html.Companion.A_HREF, value)
        return this
    }

    /** Set a placeholder name  */
    fun setHint(value: CharSequence?): Element {
        setAttribute(Html.Companion.A_PLACEHOLDER, value)
        return this
    }

    /** Apply body of element by a lambda expression.
     *
     */
    @Deprecated("Use the method {@link #next(Consumer)} rather.")
    fun then(builder: Consumer<Element?>): ExceptionProvider {
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
    fun next(builder: Consumer<Element?>): ExceptionProvider {
        try {
            builder.accept(this)
            return ExceptionProvider.Companion.of()
        } catch (e: Throwable) {
            return ExceptionProvider.Companion.of(e)
        } finally {
            close()
        }
    }

    /** String value  */
    override fun toString(): String {
        return internalElement.toString()
    }

    companion object {
        // ---- Static methods ----
        /** Crate a root element
         * @param cssLinks Nullable CSS link array
         */
        fun createHtmlRoot(title: Any, vararg cssLinks: CharSequence?): Element {
            return createHtmlRoot(title, null, *cssLinks)
        }


        /** Crate a root element
         * @param title A HTML title
         * @param charset A charset
         * @param cssLinks Nullable CSS link array
         */
        fun createHtmlRoot(
            title: Any,
            charset: Charset?,
            vararg cssLinks: CharSequence?
        ): Element {
            val result = XmlModel(Html.Companion.HTML)
            val head: XmlModel = result.addElement(Html.Companion.HEAD)
            head.addElement(Html.Companion.META).setAttribute(Html.Companion.A_CHARSET, charset)
            head.addElement(Html.Companion.TITLE).addText(title)

            if (cssLinks != null) {
                for (cssLink in cssLinks) {
                    head.addElement(Html.Companion.LINK)
                        .setAttribute(Html.Companion.A_HREF, cssLink)
                        .setAttribute(Html.Companion.A_REL, "stylesheet")
                }
            }
            return Element(result)
        }

        /** New element for an API element  */
        fun of(original: ApiElement<*>): Element {
            return if ((original is Element))
                original
            else
                Element(original)
        }
    }
}
