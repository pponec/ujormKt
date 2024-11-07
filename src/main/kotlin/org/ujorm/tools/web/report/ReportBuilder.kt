/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.report

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.web.Element
import org.ujorm.tools.web.Html
import org.ujorm.tools.web.HtmlElement
import org.ujorm.tools.web.ajax.JavaScriptWriter
import org.ujorm.tools.web.ajax.ReqestDispatcher
import org.ujorm.tools.web.ao.Column
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.ao.Injector
import org.ujorm.tools.web.json.JsonBuilder
import org.ujorm.tools.web.request.RContext
import org.ujorm.tools.web.table.ColumnModel
import org.ujorm.tools.web.table.Direction
import org.ujorm.tools.web.table.GridBuilder
import org.ujorm.tools.xml.config.HtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.io.IOException
import java.util.function.Function
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Stream

/**
 * A HTML page builder for table based report with an AJAX support.
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 * ReportBuilder.of("Hotel Report")
 * .add(Hotel::getName, "Hotel", NAME)
 * .add(Hotel::getCity, "City", CITY)
 * .add(Hotel::getStreet, "Street")
 * .build(ServletRequest, ServletResponse, resource);
</pre> *
 *
 * @author Pavel Ponec
 */
class ReportBuilder<D> @JvmOverloads constructor(
    /** Table builder config  */
    protected val config: ReportBuilderConfig<*>,
    /** Grid builder  */
    protected val gridBuilder: GridBuilder<D> = GridBuilder(
        config
    )
) {
    /** AJAX request param  */
    protected var ajaxRequestParam: HttpParameter = JavaScriptWriter.Companion.DEFAULT_AJAX_REQUEST_PARAM

    /** Extension is empty by default  */
    protected var htmlHeader: Injector = Injector { e: Element? -> }

    /** Print a config title by default  */
    protected var header: Injector = Injector { e: Element ->
        e.addHeading(
            config.config.title
        )
    }

    /** Print an empty text by default  */
    protected var footer: Injector = Injector { e: Element -> e.addText("") }

    /** Form injector  */
    protected var formAdditions: Injector = footer

    /** Javascript writer  */
    protected var javascritWriter: Supplier<Injector> = Supplier {
        JavaScriptWriter()
            .setAjax(this@ReportBuilder.ajaxEnabled)
            .setSubtitleSelector("." + config.subtitleCss)
    }

    /** is An AJAX enabled?  */
    protected var ajaxEnabled: Boolean = true

    /** Call an autosubmit on first load  */
    protected var autoSubmmitOnLoad: Boolean = false

    /** Sorted column index  */
    private var sortedColumn = -1

    constructor(title: CharSequence) : this(
        HtmlConfig.ofDefault().setTitle(title).setNiceFormat<DefaultXmlConfig>() as HtmlConfig
    )

    constructor(config: HtmlConfig) : this(ReportBuilderConfig.Companion.of(config))

    fun <V> add(column: Function<D?, V?>?): ReportBuilder<D> {
        gridBuilder.add(column)
        return this
    }

    fun <V> add(column: Function<D?, V?>?, title: CharSequence?): ReportBuilder<D> {
        gridBuilder.add(column, title)
        return this
    }

    fun <V> add(column: Function<D?, V?>?, title: Injector?): ReportBuilder<D> {
        gridBuilder.add(column, title!!)
        return this
    }

    fun <V> add(column: Function<D?, V?>?, title: CharSequence?, param: HttpParameter?): ReportBuilder<D> {
        gridBuilder.add(column, title, param)
        return this
    }

    fun <V> add(column: Function<D?, V?>?, title: Injector?, param: HttpParameter?): ReportBuilder<D> {
        gridBuilder.add(column, title!!, param)
        return this
    }

    fun addColumn(column: Column<D?>, title: CharSequence): ReportBuilder<D> {
        gridBuilder.add(column, title)
        return this
    }

    fun addColumn(column: Column<D?>, title: Injector): ReportBuilder<D> {
        gridBuilder.add(column, title)
        return this
    }

    /** Add new column for a row counting  */
    fun addOrder(title: CharSequence): ReportBuilder<D> {
        gridBuilder.addOrder(title)
        return this
    }

    /** Get column model by index  */
    fun getColumn(index: Int): ColumnModel<D?, *>? {
        return gridBuilder.getColumn(index)
    }

    val columnSize: Int
        /** Returns a count of columns  */
        get() = gridBuilder.columnSize

    /**
     * Add a sortable indicator to the last column model
     * @return
     */
    fun <V> sortable(): ReportBuilder<D> {
        gridBuilder.sortable<Any>()
        return this
    }

    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    fun <V> sortable(ascending: Boolean): ReportBuilder<D> {
        gridBuilder.sortable<Any>(ascending)
        return this
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The `null` value shows an unused sorting action.
     * @return
     */
    fun <V> sortable(direction: Direction): ReportBuilder<D> {
        gridBuilder.sortable<Any>(direction)
        return this
    }

    /** Get sorted column or a stub if the sorted column not found  */
    fun getSortedColumn(): ColumnModel<D?, *> {
        return gridBuilder.sortedColumn
    }

    fun setAjaxRequestParam(ajaxRequestParam: HttpParameter): ReportBuilder<D> {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam")
        return this
    }

    fun setHtmlHeader(htmlHeader: Injector): ReportBuilder<D> {
        this.htmlHeader = Assert.notNull(htmlHeader, "htmlHeader")
        return this
    }

    fun setHeader(header: Injector): ReportBuilder<D> {
        this.header = Assert.notNull(header, "header")
        return this
    }

    fun setFooter(footer: Injector): ReportBuilder<D> {
        this.footer = Assert.notNull(footer, "footer")
        return this
    }

    fun setFormItem(formItem: Injector): ReportBuilder<D> {
        this.formAdditions = Assert.notNull(formItem, "formAdditions")
        return this
    }

    /** Enable of disable an AJAX feature, default value si `true`  */
    fun setAjaxEnabled(ajaxEnabled: Boolean): ReportBuilder<D> {
        this.ajaxEnabled = ajaxEnabled
        return this
    }

    fun setJavascritWriter(javascritWriter: Supplier<Injector>): ReportBuilder<D> {
        this.javascritWriter = Assert.notNull(javascritWriter, "javascritWriter")
        return this
    }

    @Throws(IllegalStateException::class)
    fun setEmbeddedIcons(embeddedIcons: Boolean): ReportBuilder<D> {
        if (config is ReportBuilderConfigImpl<*>) {
            config.setEmbeddedIcons(embeddedIcons)
        } else {
            throw IllegalStateException("Configuration must be type of: " + ReportBuilderConfigImpl::class.java)
        }
        return this
    }

    /** Build the HTML page including a table  */
    fun build(
        context: RContext,
        resource: Stream<D?>
    ) {
        build(
            context
        ) { tableBuilder: GridBuilder<D?>? -> resource }
    }

    /** Build the HTML page including a table  */
    fun build(
        context: RContext,
        resource: Function<GridBuilder<D?>?, Stream<D?>?>
    ) {
        try {
            setSort(ColumnModel.Companion.ofCode(config.sortRequestParam.of(context)))
            ReqestDispatcher(context, config.config)
                .onParam(
                    config.ajaxRequestParam
                ) { jsonBuilder: JsonBuilder -> doAjax(context, jsonBuilder, resource) }
                .onDefaultToElement { element: HtmlElement? ->
                    printHtmlBody(
                        context,
                        element!!, resource
                    )
                }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Internal server error", e)
            throw IllegalStateException("500") // TODO.pop
        }
    }

    /** Mark a column as sortable  */
    protected fun setSort(sort: ColumnModel<*, *>) {
        this.sortedColumn = sort.index
        if (sortedColumn >= 0) {
            val i = intArrayOf(-1)
            gridBuilder.columns.forEach { cm: ColumnModel<D?, *>? ->
                ++i[0]
                if (cm!!.isSortable) {
                    cm.direction = if (sort.index == i[0])
                        sort.direction
                    else
                        Direction.NONE
                }
            }
        }
    }

    protected fun printHtmlBody(
        context: RContext,
        html: HtmlElement,
        resource: Function<GridBuilder<D?>?, Stream<D?>?>
    ) {
        Assert.notNull(context, "context")
        Assert.notNull(html, "html")
        Assert.notNull(resource, "resource")

        if (Check.hasLength(config.javascriptLink)) {
            html.addJavascriptLink(false, config.javascriptLink)
        }
        html.addCssLink(config.cssLink)
        config.cssWriter.accept(html.head, gridBuilder.isSortable)
        javascritWriter.get().write(html.head)
        htmlHeader.write(html.head)
        html.body.use { body ->
            header.write(body)
            body.addDiv(config.subtitleCss).addText(if (ajaxEnabled) config.ajaxReadyMessage else "")
            body.addForm()
                .setId(config.formId)
                .setMethod(Html.Companion.V_POST).setAction("?").use { form ->
                    gridBuilder.columns.forEach { column: ColumnModel<D?, *>? ->
                        if (column!!.isFiltered) {
                            val param = column.getParam(UNDEFINED_PARAM)
                            form.addTextInp(
                                param,
                                param.of(context),
                                column.title,
                                config.controlCss,
                                column.getParam(UNDEFINED_PARAM)
                            )
                        }
                    }
                    // Hidden submit button is important if a javascript is disabled:
                    form.addInput().setType(Html.Companion.V_SUBMIT).setAttribute(Html.Companion.V_HIDDEN)
                    if (gridBuilder.isSortable) {
                        printSortedField(form.addSpan().setId(config.sortRequestParam), context)
                    }
                    formAdditions.write(form)
                    // Add the table:
                    val tableCss = config.tableCssClass
                    printTableBody(form.addTable(*tableCss.toTypedArray<CharSequence?>()), context, resource)
                }
            footer.write(body)
        }
    }

    /** The hidden field contains an index of the last sorted column  */
    protected fun printSortedField(parent: Element, context: RContext) {
        val index = config.sortRequestParam.of(context, -1)
        parent.addInput().setAttribute(Html.Companion.A_TYPE, Html.Companion.V_HIDDEN)
            .setNameValue(config.sortRequestParam, index)
    }

    protected fun printTableBody(
        table: Element,
        context: RContext,
        resource: Function<GridBuilder<D?>?, Stream<D?>?>
    ) {
        val sortedColumn: ColumnModel<*, *> = ColumnModel.Companion.ofCode(
            config.sortRequestParam.of(context)
        )
        gridBuilder.build(table, sortedColumn, resource)
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param context servlet context
     * @param output A JSON writer
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    protected fun doAjax(
        context: RContext,
        output: JsonBuilder,
        resource: Function<GridBuilder<D?>?, Stream<D?>?>
    ) {
        output.writeClass(
            config.tableSelector
        ) { e: Element -> printTableBody(e, context, resource) }
        output.writeClass(config.subtitleCss, config.ajaxReadyMessage)
        if (gridBuilder.isSortable) {
            output.writeId(
                config.sortRequestParam
            ) { e: Element -> printSortedField(e, context) }
        }
    }

    /** URL constants  */
    class Url @JvmOverloads constructor(bootstrapCss: String = BOOTSTRAP_CSS, jQueryJs: String = JQUERY_JS) {
        val bootstrapCss: String =
            Assert.hasLength(bootstrapCss, "bootstrapCss")

        companion object {
            /** Link to a Bootstrap URL of CDN  */
            protected const val BOOTSTRAP_CSS: String =
                "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css"

            /** Link to jQuery of CDN  */
            protected const val JQUERY_JS: String = ""
        }
    }

    companion object {
        /** Logger  */
        private val LOGGER: Logger = Logger.getLogger(ReportBuilder::class.java.name)

        /** An undefined parameter  */
        private val UNDEFINED_PARAM: HttpParameter = HttpParameter.Companion.of("UNDEFINED_PARAM")
    }
}
