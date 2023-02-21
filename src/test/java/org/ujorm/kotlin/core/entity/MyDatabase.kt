package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.impl.AbstractEntityProvider

/** Common entity provider */
object MyDatabase : AbstractEntityProvider() {

    /** Initialize and close the entity model */
    fun closeModel() = super.close<MyDatabase>()
}