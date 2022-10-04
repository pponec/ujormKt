package org.ujorm.kotlin.entityComposed.entity

import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.entityComposed.DomainEntityModel
import java.time.LocalDate

/** An user entity */
data class Employee constructor(
    var id: Int,
    var name: String,
    var contractDay: LocalDate,
    var department: Department = Department(2, "D"),
    var supervisor: Employee? = null
)

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
    /** Direct property model */
    override val core: _Employees = _Employees().close()

    val id get() = property(core.id)
    val name get() = property(core.name)
    val contractDay get() = property(core.contractDay)
    val department: Departments<D> get() = property(core.department)
    val supervisor: Employees<D> get() = property(core.supervisor)
}

/** Initialize, register and close the entity model. */
val EntityProvider.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees<Employee>().close() as Employees<Employee>
}