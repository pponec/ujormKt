/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.msg.MsgFormatter
import org.ujorm.tools.web.ao.Column
import org.ujorm.tools.web.ao.HttpParameter
import java.io.IOException
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * Table column model
 *
 * @author Pavel Ponec
 */
class ColumnModel<D, V>(
    val index: Int,
    column: Function<D, V?>,
    title: CharSequence,
    val param: HttpParameter?
) {
    val column: Function<D, V>
    val title: CharSequence

    /** Is the column sortable?  */
    var isSortable: Boolean = false
        private set
    private var direction = Direction.NONE

    constructor(direction: Direction, index: Int) : this(
        index,
        Function<D, V?> { x: D -> null }, "", null
    ) {
        setSortable(direction)
    }

    init {
        this.column = Assert.notNull(column, "column")
        this.title = Assert.notNull(title, "title")
    }

    fun getParam(defaultValue: HttpParameter): HttpParameter {
        return param ?: defaultValue
    }

    fun getDirection(): Direction {
        return direction
    }

    val isFiltered: Boolean
        get() = param != null

    fun setSortable(direction: Direction) {
        this.isSortable = true
        setDirection(direction)
    }

    fun setDirection(direction: Direction) {
        this.direction = Assert.notNull(direction, "direction")
    }

    /**
     * Write the content to an appendable text stream
     */
    fun toCode(opposite: Boolean): String {
        try {
            return toCode(opposite, StringBuilder(4)).toString()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Write the content to an appendable text stream where the default direction is an ASCENDING.
     */
    @Throws(IOException::class)
    fun toCode(opposite: Boolean, writer: Appendable): Appendable {
        val coeff = if ((Direction.ASC.safeEquals(direction) == opposite)) -1 else 1
        writer.append((coeff * (index + 1)).toString())
        return writer
    }

    /** Get comparator of a sortable column  */
    fun getComparator(defaultFce: Function<D, *>?): Comparator<D> {
        return getComparator(Comparator.comparing<Any, Comparable<*>>(defaultFce as Function<*, *>?))
    }

    /** Get comparator of a sortable column  */
    fun getComparator(defaultCompar: Comparator<D>): Comparator<D> {
        if (isSortable && isIncludeColumnType) {
            val compar: Comparator<D> = Comparator.comparing<Any, Comparable<*>>(column as Function<*, *>)
            when (direction) {
                Direction.ASC -> return compar
                Direction.DESC -> return compar.reversed()
            }
        }
        return defaultCompar
    }

    protected val isIncludeColumnType: Boolean
        /** Including is more common choice  */
        get() = if (true) {
            true
        } else {
            column !is Column<*>
        }

    override fun toString(): String {
        return MsgFormatter.format("[{}]:{}:{}", index, title, if (isSortable) direction.name else "-")
    }

    companion object {
        private val x: NullPointerException? = null

        /** Number pattern  */
        private val NUMBER: Pattern = Pattern.compile("-?\\d+")

        fun ofCode(paramValue: String): ColumnModel<*, *> {
            if (NUMBER.matcher(paramValue).matches()) {
                val intCode = paramValue.toInt()
                val direction: Direction = Direction.Companion.of(intCode > 0)
                return ColumnModel<Any, Any>(
                    direction,
                    (abs(intCode.toDouble()) - 1).toInt()
                )
            } else {
                return ColumnModel<Any, Any>(Direction.NONE, -1)
            }
        }

        /** Create a stub column  */
        fun <D, V> ofStub(): ColumnModel<D, V?> {
            return ColumnModel(
                -1,
                { x: D -> null }, "", null
            )
        }
    }
}
