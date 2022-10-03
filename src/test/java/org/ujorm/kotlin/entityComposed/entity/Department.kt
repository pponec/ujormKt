package org.ujorm.kotlin.entityComposed.entity

import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.entityComposed.DomainEntityModel
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
open class Departments<D : Any> : DomainEntityModel<D, Department>() {

    /** Direct property model */
    override val core: _Departments = _Departments().close()

    // --- Properties ---

    val id get() = property(core.id)
    val name get() = property(core.name)
    val created get() = property(core.created)

    /** Initialize, register and close the entity model. */
    val EntityProvider.departments by lazy {
        Departments<Department>().close() as Departments<Department>
    }
}

