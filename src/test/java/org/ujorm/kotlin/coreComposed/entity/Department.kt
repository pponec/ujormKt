package org.ujorm.kotlin.coreComposed.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.coreComposed.DomainEntityModel
import java.time.LocalDate


/** A Department entity */
@Entity
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
}

/** Model of the entity can be a generated class in the feature */
open class _Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}

/** Model of the entity can be a generated class in the feature */
open class Departments<D : Any> : DomainEntityModel<D, Department>() {
    override val core: _Departments = _Departments().close()

    // --- Properties ---

    val id get() = property(core.id)
    val name get() = property(core.name)
    val created get() = property(core.created)
}

/** Initialize, register and close the entity model. */
val Entities.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments<Department>() //.close() as Departments<Department>
}