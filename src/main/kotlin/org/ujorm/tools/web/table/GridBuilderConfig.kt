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
package org.ujorm.tools.web.table

import org.ujorm.tools.web.Element
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.report.ReportBuilderConfigImpl
import org.ujorm.tools.xml.config.HtmlConfig
import java.io.InputStream
import java.time.Duration
import java.util.function.BiConsumer

/**
 * A HTML page builder for table based an AJAX.
 *
 * <h3>Usage<h3>
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
</h3></h3> */
interface GridBuilderConfig<D> {
    val config: HtmlConfig

    val cssLink: String

    /** Link to an external Javascript library where a no-library returns an empty String  */
    val javascriptLink: String

    val idleDelay: Duration

    val ajaxRequestParam: HttpParameter

    val sortRequestParam: HttpParameter

    val ajaxReadyMessage: CharSequence

    val formId: String

    val controlCss: String

    val subtitleCss: String

    val tableSelector: CharSequence

    val tableCssClass: List<CharSequence?>

    val sortable: CharSequence

    val sortableAsc: CharSequence

    val sortableDesc: CharSequence

    val sortableBoth: CharSequence

    /** Use inner icons for sortable images  */
    val isEmbeddedIcons: Boolean

    /** Inline CSS writer where the first method is an Element and the seconnd one is a sortable   */
    val cssWriter: BiConsumer<Element?, Boolean?>

    /** Get a CSS direction style  */
    fun getSortableDirection(direction: Direction): CharSequence {
        return when (direction) {
            Direction.ASC -> sortableAsc
            Direction.DESC -> sortableDesc
            Direction.NONE -> sortableBoth
            else -> throw IllegalArgumentException("Unsupported $direction")
        }
    }

    /** Get a CSS direction style  */
    fun getInnerSortableImageToStream(direction: Direction): InputStream? {
        return javaClass.getResourceAsStream(getInnerSortableImage(direction))
    }

    /** Get a CSS direction style  */
    fun getInnerSortableImage(direction: Direction): String {
        val baseDir = "/META-INF/resources/org/ujorm/images/v1/order"
        return when (direction) {
            Direction.ASC -> java.lang.String.join("/", baseDir, "up.png")
            Direction.DESC -> java.lang.String.join("/", baseDir, "down.png")
            Direction.NONE -> java.lang.String.join("/", baseDir, "both.png")
            else -> throw IllegalArgumentException("Unsupported $direction")
        }
    }

    companion object {
        /** Returns a default implementation  */
        fun of(config: HtmlConfig): GridBuilderConfig<*> {
            return ReportBuilderConfigImpl<Any?>(config)
        }
    }
}
