package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.impl.AbstractEntityProvider

/** Entity provider */
object Entities : AbstractEntityProvider() {

    /** Initialize and close the entity model */
    fun closeModel() = super.close<Entities>()
}