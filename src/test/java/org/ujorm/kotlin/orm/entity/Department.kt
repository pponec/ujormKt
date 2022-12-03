package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.anotation.OneToMany
import org.ujorm.kotlin.core.impl.EntityModel
import org.ujorm.kotlin.coreComposed.DomainEntityModel
import java.time.LocalDate

/** A Department entity */
@Entity
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
    @OneToMany(criterion = SeniorEmployeeFilter::class)
    var members: List<Employee>
}

/** Model of the entity can be a generated class in the feature */
open class _Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { org.ujorm.kotlin.core.entity.Department::created } // Alternative notation
    val members = propertyList { Employee::department } // Notation for a Relation 1:M
}

/** Model of the entity can be a generated class in the feature */
open class Departments<D : Any> : DomainEntityModel<D, Department>() {
    override val core: _Departments = _Departments().close()

    // --- Entity Properties ---

    val id get() = property(core.id)
    val name get() = property(core.name)
    val created get() = property(core.created)
}

/** Initialize, register and close the entity model. */
val Database.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments<Department>().close() as Departments<Department>
}