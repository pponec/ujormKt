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

import org.ujorm.tools.Assert
import org.ujorm.tools.Check
import org.ujorm.tools.web.Element
import org.ujorm.tools.web.Html
import org.ujorm.tools.web.ao.Column
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.ao.Injector
import org.ujorm.tools.web.ao.WebUtils
import org.ujorm.tools.xml.ApiElement
import org.ujorm.tools.xml.config.HtmlConfig
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Function
import java.util.logging.Logger
import java.util.stream.Stream

/**
 * Build a content of a HTML page for a sortable data grid.
 *
 * @author Pavel Ponec
 */
class GridBuilder<D>(
    /** Table builder config  */
    protected val config: GridBuilderConfig<*>
) {
    /** Columns  */
    protected val columns: MutableList<ColumnModel<D?, *>> = ArrayList()

    /** An order of sorted column whete a negavive value means a descending direction  */
    private var sortedColumn = -1

    /** Is the table sortable  */
    var isSortable: Boolean? = null
        /** Returns the true in case the table is sortable.
         *
         * NOTE: Calculated result is cached, call the method on a final model only!
         */
        get() {
            if (field == null) {
                field = isSortableCalculated
            }
            return field
        }
        private set

    constructor(title: CharSequence) : this(
        HtmlConfig.ofDefault().setTitle(title).setNiceFormat<DefaultXmlConfig>() as HtmlConfig
    )

    constructor(config: HtmlConfig) : this(GridBuilderConfig.Companion.of(config))

    fun <V> add(column: Function<D?, V>): GridBuilder<D> {
        return addInternal<V>(column, "Column-" + (columns.size + 1), null)
    }

    fun <V> add(column: Function<D?, V>, title: CharSequence): GridBuilder<D> {
        return addInternal<V>(column, title, null)
    }

    fun <V> add(column: Function<D?, V>, title: Injector): GridBuilder<D> {
        return addInternal<V>(column, title, null)
    }

    fun <V> add(column: Function<D?, V>, title: CharSequence, param: HttpParameter?): GridBuilder<D> {
        return addInternal<V>(column, title, param)
    }

    fun <V> add(column: Function<D?, V>, title: Injector, param: HttpParameter?): GridBuilder<D> {
        return addInternal<V>(column, title, param)
    }

    fun addColumn(column: Column<D?>, title: CharSequence): GridBuilder<D> {
        return addInternal(column, title, null)
    }

    fun addColumn(column: Column<D?>, title: Injector): GridBuilder<D> {
        return addInternal(column, title, null)
    }

    /** Add new column for a row counting  */
    fun addOrder(title: CharSequence): GridBuilder<D> {
        val textRight = "text-right"
        return addColumn(object : Column<D> {
            val order: AtomicLong = AtomicLong()
            override fun write(e: Element, row: D) {
                e.setClass(Html.Companion.A_CLASS, textRight).addText(apply(row), '.')
            }

            override fun apply(t: D): Any {
                return order.incrementAndGet()
            }
        },
            Injector { e: Element -> e.setClass(Html.Companion.A_CLASS, textRight).addText(title) })
    }

    protected fun <V> addInternal(
        column: Function<D?, V?>,
        title: CharSequence,
        param: HttpParameter?
    ): GridBuilder<D> {
        columns.add(ColumnModel<Any?, Any?>(columns.size, column, title, param))
        return this
    }

    /** Get column model by index  */
    fun getColumn(index: Int): ColumnModel<D?, *> {
        return columns[index]
    }

    val columnSize: Int
        /** Returns a count of columns  */
        get() = columns.size

    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    fun <V> sortable(ascending: Boolean): GridBuilder<D> {
        return sortable<Any>(if (ascending) Direction.ASC else Direction.DESC)
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The `null` value shows an unused sorting action.
     * @return
     */
    /**
     * Add a sortable indicator to the last column model
     * @return
     */
    @JvmOverloads
    fun <V> sortable(direction: Direction = Direction.NONE): GridBuilder<D> {
        Assert.notNull(direction, "direction")
        Assert.hasLength(columns, "No column is available")
        columns[columns.size - 1].setSortable(direction)
        return this
    }

    /** Get sorted column or a stub of the sorted column was not found  */
    fun getSortedColumn(): ColumnModel<D?, *> {
        return if ((sortedColumn >= 0 && sortedColumn < columnSize))
            getColumn(sortedColumn)
        else
            ColumnModel.Companion.ofStub<D, Any>()
    }

    //    public GridBuilder<D> setEmbeddedIcons(boolean embeddedIcons) throws IllegalStateException {
    //        if (config instanceof ReportBuilderConfigImpl) {
    //            ((ReportBuilderConfigImpl)config).setEmbeddedIcons(embeddedIcons);
    //        } else {
    //            throw new IllegalStateException("Configuration must be type of: " + ReportBuilderConfigImpl.class);
    //        }
    //        return this;
    //    }
    /** Build the HTML page including a table  */
    fun build(
        parent: ApiElement<*>,
        resource: Function<GridBuilder<D>?, Stream<D>>
    ) {
        printTable(Element.Companion.of(parent), resource)
    }

    /** Build the HTML page including a table  */
    fun build(
        parent: ApiElement<*>,
        sortedColumn: ColumnModel<*, *>,
        resource: Function<GridBuilder<D>?, Stream<D>>
    ) {
        // An original code: setSort(ColumnModel.ofCode(config.getSortRequestParam().of(input)));

        setSort(Assert.notNull(sortedColumn, "sortedColumn"))
        printTable(Element.Companion.of(parent), resource)
    }

    /** Mark a column as sorted  */
    protected fun setSort(sort: ColumnModel<*, *>) {
        this.sortedColumn = sort.index
        if (sortedColumn >= 0) {
            var i = 0
            val max = columns.size
            while (i < max) {
                val cm: ColumnModel<*, *> = columns[i]
                if (cm.isSortable) {
                    cm.direction = if (sort.index == i) sort.direction else Direction.NONE
                }
                i++
            }
        }
    }

    /**
     * Print table
     * @param parent If a name of the element is a `"table"` or an empty text
     * then do not create new table element.
     * @param resource Data source
     */
    protected fun printTable(
        parent: Element,
        resource: Function<GridBuilder<D>?, Stream<D>>
    ) {
        val elementName = parent.name
        val table = if ((Check.isEmpty(elementName) || Html.Companion.TABLE == elementName))
            parent
        else
            parent.addTable()
        val headerRow = table.addElement(Html.Companion.THEAD).addElement(Html.Companion.TR)
        for (col in columns) {
            val columnSortable = col.isSortable
            val value: Any = col.title
            val th = headerRow.addElement(Html.Companion.TH)
            val thLink = if (columnSortable)
                th.addSubmitButton(
                    config.sortable,
                    config.getSortableDirection(col.direction)
                )
                    .setAttribute(Html.Companion.A_NAME, config.sortRequestParam)
                    .setAttribute(Html.Companion.A_VALUE, col.toCode(true))
            else
                th
            if (value is Injector) {
                value.write(thLink)
            } else {
                thLink.addText(value)
            }
            if (columnSortable && config.isEmbeddedIcons) {
                val img = config.getInnerSortableImageToStream(col.direction)
                if (img != null) {
                    thLink.addImage(img, col.direction.toString())
                }
            }
        }
        table.addElement(Html.Companion.TBODY).use { tBody ->
            val cols: Any = columns.stream().map { t: ColumnModel<D?, *> -> t.column }
            val hasRenderer = WebUtils.isType(Column::class.java, cols)
            resource.apply(this).forEach { value: D ->
                val rowElement = tBody.addElement(Html.Companion.TR)
                for (col in columns) {
                    val attribute = col.column
                    val td = rowElement.addElement(Html.Companion.TD)
                    if (hasRenderer && attribute is Column<*>) {
                        (attribute as Column<*>).write(td, value)
                    } else {
                        td.addText(attribute.apply(value))
                    }
                }
            }
        }
    }

    val isSortableCalculated: Boolean
        /** Calculate if the table has an sortable column  */
        get() {
            for (column in columns) {
                if (column.isSortable) {
                    return true
                }
            }
            return false
        }

    /** Returns all table columns in a stream  */
    fun getColumns(): Stream<ColumnModel<D?, *>> {
        return columns.stream()
    }

    companion object {
        /** Logger  */
        private val LOGGER: Logger = Logger.getLogger(GridBuilder::class.java.name)
    }
}
