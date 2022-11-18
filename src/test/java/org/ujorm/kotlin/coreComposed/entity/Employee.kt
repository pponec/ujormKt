package org.ujorm.kotlin.coreComposed.entity

import org.ujorm.kotlin.core.Entity
import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.coreComposed.DomainEntityModel
import java.time.LocalDate

/** An user entity */
interface Employee : Entity<Employee> {
    var id: Int
    var name: String
    var contractDay: LocalDate
    var department: Department
    var supervisor: Department?
}

/** Model of the entity can be a generated class in the feature */
open class _Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Model of the entity can be a generated class in the feature */
open class Employees<D : Any>() : DomainEntityModel<D, Employee>() {
    override val core: _Employees = _Employees().close()

    val id get() = property(core.id)
    val name get() = property(core.name)
    val contractDay get() = property(core.contractDay)
    val department: Departments<D> get() = property(core.department)
    val supervisor: Employees<D> get() = property(core.supervisor)
}

/** Initialize, register and close the entity model. */
val Entities.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees<Employee>() //.close() as Employees<Employee>
}