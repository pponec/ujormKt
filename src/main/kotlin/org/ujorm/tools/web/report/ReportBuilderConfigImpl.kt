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
import org.ujorm.tools.web.Element
import org.ujorm.tools.web.Html
import org.ujorm.tools.web.ajax.JavaScriptWriter
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.xml.config.HtmlConfig
import java.time.Duration
import java.util.function.BiConsumer
import java.util.logging.Logger

/**
 * A HTML page builder for table based an AJAX.
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 * TableBuilder.of("Hotel Report")
 * .add(Hotel::getName, "Hotel", NAME)
 * .add(Hotel::getCity, "City", CITY)
 * .add(Hotel::getStreet, "Street")
 * .build(ServletRequest, ServletResponse, resource);
</pre> *
 *
 * @author Pavel Ponec
 */
class ReportBuilderConfigImpl<D> protected constructor(
    /** HTML config  */
    override val config: HtmlConfig,
    /** Link to CSS file  */
    override var cssLink: String,
    /** Link to an external JavaScript library where no-library returns an empty String  */
    override var javascriptLink: String,
    /** Iddle delay in millis  */
    override var idleDelay: Duration,
    /** AJAX request param  */
    override var ajaxRequestParam: HttpParameter,
    /** AJAX request param  */
    override var sortRequestParam: HttpParameter,
    /** Form identifier  */
    override var formId: String,
    /** Bootstrap form control CSS class name  */
    override var controlCss: String,
    /** CSS class name for the output box  */
    override var subtitleCss: String,
    /** Table CSS class  */
    override var tableCssClass: List<CharSequence?>,
    sortableColumn: String,
    sortableAsc: String,
    sortableDesc: String,
    sortableBoth: String,
    embeddedIcons: Boolean,
    cssWriter: BiConsumer<Element?, Boolean?>
) : ReportBuilderConfig<D> {
    // --- GETTERS ---

    /** Link to an external Javascript library  */

    /** AJA ready param  */
    override var ajaxReadyMessage: CharSequence = "AJAX ready"
        private set

    /** Table selector  */
    override var tableSelector: CharSequence? = null
    /** Sortable CSS class  */
    /** Sortable column CSS style  */
    override val sortable: CharSequence = sortableColumn
    /** Sortable ascending CSS class  */
    /** Sortable column ascending CSS style  */
    override val sortableAsc: CharSequence = sortableAsc
    /** Sortable descending CSS class  */
    /** Sortable column descending CSS style  */
    override val sortableDesc: CharSequence = sortableDesc
    /** Sortable both CSS class  */
    /** Sortable column undefined CSS style  */
    override val sortableBoth: CharSequence = sortableBoth
    /** Use an external images for sortable icons  */
    /** Use an external images for sortable icons  */
    override var isEmbeddedIcons: Boolean
        private set

    /** Inline CSS writer  */
    override var cssWriter: BiConsumer<Element?, Boolean?>?

    constructor(config: HtmlConfig) : this(
        config,  // config
        Constants.BOOTSTRAP_CSS,  // cssLink
        "",  // jQueryLink
        Constants.IDLE_DELAY,  // idleDelay
        JavaScriptWriter.Companion.DEFAULT_AJAX_REQUEST_PARAM,  // ajaxRequestParam
        JavaScriptWriter.Companion.DEFAULT_SORT_REQUEST_PARAM,  // sortRequestParam
        Constants.FORM_ID,  // formId
        Constants.CONTROL_CSS,  // controlCss
        Constants.SUBTITLE_CSS,  // subtitleCss
        Constants.TABLE_CSS_CLASS,  // tableCssClass
        "sortable",  // sortableColumn
        "asc",  // sortableAsc
        "desc",  // sortableDesc
        "both",  // sortableBoth
        false,  // embeddedIcons
        null // cssWriter
    )

    init {
        this.isEmbeddedIcons = embeddedIcons
        this.cssWriter = cssWriter
    }

    protected val tableClassSelector: CharSequence
        /** Returns a fist class of table element by defult  */
        get() = if (tableCssClass.isEmpty())
            Html.Companion.TABLE
        else
            java.lang.String.join(" .", Html.Companion.TABLE, tableCssClass[0])

    fun setCssLink(cssLink: String): ReportBuilderConfigImpl<D> {
        this.cssLink = Assert.notNull(cssLink, "cssLink")
        return this
    }

    fun setJqueryLink(jqueryLink: String): ReportBuilderConfigImpl<D> {
        this.javascriptLink = Assert.notNull(jqueryLink, "jqueryLink")
        return this
    }

    fun setIdleDelay(idleDelay: Duration): ReportBuilderConfigImpl<D> {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay")
        return this
    }

    fun setAjaxRequestParam(ajaxRequestParam: HttpParameter): ReportBuilderConfigImpl<D> {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam")
        return this
    }

    fun setSortRequestParam(sortRequestParam: HttpParameter): ReportBuilderConfigImpl<D> {
        this.sortRequestParam = Assert.notNull(sortRequestParam, "sortRequestParam")
        return this
    }

    fun setAjaxReadyMessage(ajaxReadyMessage: CharSequence): ReportBuilderConfigImpl<D> {
        this.ajaxReadyMessage = Assert.hasLength(ajaxReadyMessage, "ajaxReadyMessage")
        return this
    }

    fun setFormId(formId: String): ReportBuilderConfigImpl<D> {
        this.formId = Assert.hasLength(formId, "formId")
        return this
    }

    fun setControlCss(controlCss: String): ReportBuilderConfigImpl<D> {
        this.controlCss = Assert.hasLength(controlCss, "controlCss")
        return this
    }

    fun setSubtitleCss(subtitleCss: String): ReportBuilderConfigImpl<D> {
        this.subtitleCss = Assert.hasLength(subtitleCss, "subtitleCss")
        return this
    }

    fun setTableSelector(tableSelector: CharSequence): ReportBuilderConfigImpl<D> {
        this.tableSelector = Assert.notNull(tableSelector, "tableSelector")
        return this
    }

    fun setTableCssClass(tableCssClass: List<CharSequence?>): ReportBuilderConfigImpl<D> {
        this.tableCssClass = Assert.notNull(tableCssClass, "tableCssClass")
        return this
    }

    /** Inline CSS writer  */
    fun setCssWriter(cssWriter: BiConsumer<Element?, Boolean?>?): ReportBuilderConfigImpl<D> {
        this.cssWriter = cssWriter
        return this
    }

    /** Use an external images for sortable icons  */
    fun setEmbeddedIcons(embeddedIcons: Boolean): Boolean {
        return embeddedIcons.also { this.isEmbeddedIcons = it }
    }

    override fun getTableSelector(): CharSequence {
        return if (tableSelector != null) tableSelector!! else tableCssClass[0]!!
    }

    /** Inline CSS writer where a default value is generated from the [.inlineCssWriter] method.
     * }  */
    override fun getCssWriter(): BiConsumer<Element?, Boolean?> {
        return if (cssWriter != null) cssWriter!! else inlineCssWriter()
    }

    /** Default header CSS style printer  */
    protected fun inlineCssWriter(): BiConsumer<Element, Boolean> {
        return BiConsumer<Element, Boolean> { element: Element, sortable: Boolean ->
            val conf: ReportBuilderConfig<*> =
                this
            val newLine = conf.config.newLine
            element.addElement(Html.Companion.STYLE).use { css ->
                css.addRawText(newLine, "body { margin: 10px;}")
                css.addRawText(newLine, ".", conf.subtitleCss, " {font-size: 10px; color: silver;}")
                css.addRawText(newLine, "#", conf.formId, " {margin-bottom: 2px;}")
                css.addRawText(newLine, "#", conf.formId, " input {width: 200px;}")
                css.addRawText(newLine, ".", conf.controlCss, " {display: inline;}")
                css.addRawText(newLine, ".table th {background-color: #e8e8e8;}")
                css.addRawText(
                    newLine,
                    "button.",
                    conf.sortable,
                    " {border: none; padding: 0;background: none; font-weight: bold;}"
                )
                if (java.lang.Boolean.TRUE == sortable) {
                    if (isEmbeddedIcons) {
                        css.addRawText(newLine, ".sortable img {margin-left: 6px;} ")
                    } else {
                        val img = "/org/ujorm/images/v1/order/"
                        css.addRawText(
                            newLine, "button.", conf.sortable,
                            (" {background-repeat: no-repeat;"
                                    + " background-position: right;"
                                    + " padding-right: 14px;"
                                    + " color: #212529;}")
                        )
                        css.addRawText(
                            newLine,
                            ".",
                            conf.sortable,
                            ".",
                            conf.sortableAsc,
                            " {background-image: url('",
                            img,
                            "up",
                            ".png')}"
                        )
                        css.addRawText(
                            newLine,
                            ".",
                            conf.sortable,
                            ".",
                            conf.sortableDesc,
                            " {background-image: url('",
                            img,
                            "down",
                            ".png')}"
                        )
                        css.addRawText(
                            newLine,
                            ".",
                            conf.sortable,
                            ".",
                            conf.sortableBoth,
                            " {background-image: url('",
                            img,
                            "both",
                            ".png')}"
                        )
                    }
                }
            }
        }
    }

    /** Config constants  */
    object Constants {
        /** Link to a Bootstrap URL of CDN  */
        const val BOOTSTRAP_CSS: String = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css"

        /** Form identifier  */
        const val FORM_ID: String = "form"

        /** Bootstrap form control CSS class name  */
        const val CONTROL_CSS: String = "form-control"

        /** CSS class name for the output box  */
        const val SUBTITLE_CSS: String = "subtitle"

        /** Table CSS classes  */
        var TABLE_CSS_CLASS: List<CharSequence?> =
            mutableListOf<CharSequence?>("table", "table-striped", "table-bordered")

        /** Key delay  */
        val IDLE_DELAY: Duration = Duration.ofMillis(250)
    }

    companion object {
        /** Logger  */
        private val LOGGER: Logger = Logger.getLogger(ReportBuilderConfigImpl::class.java.name)
    }
}
