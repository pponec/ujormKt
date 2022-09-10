package org.ujorm.kotlin.entityComposed.entity

import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.core.PropertyNullable
import org.ujorm.kotlin.model.DomainEntityModel
import java.time.LocalDate


/** An Department entity */
data class Department constructor(
    var id: Int,
    var name: String,
    var created: LocalDate = LocalDate.now().minusDays(1),
)

/** Model of the entity can be a generated class in the feature */
open class _Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}


/** Model of the entity can be a generated class in the feature */
open class Departments<D : Any>() : DomainEntityModel<Department>(_Departments()) {

    /** Direct property model */
    private val core = _Departments().close() as _Departments

    fun core() = this.core

    /** Build the new Property */
    protected fun <V : Any> property(property : PropertyNullable<*, V>) : PropertyNullable<D, V> {
        return if (super.baseInstance) {
            property as PropertyNullable<D, V>
        } else {
            TODO("Create composed property")
        }
    }

    /** Clone the model for the new domain */
    fun <P : Any> basedOn(prefixedDomain: PropertyNullable<P, D>): Departments<P> {
       return null !!
    }

    // --- Properties ---

    val id get() = property(core.id)
    val name get() = property(core.name)
    val created get() = property(core.created)
}

/** Initialize, register and close the entity model. */
val ModelProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments<Department>().close() as Departments<Department>
}