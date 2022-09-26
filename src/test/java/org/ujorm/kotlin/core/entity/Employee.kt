package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.EntityModel
import java.time.LocalDate

/** An user entity */
data class Employee constructor(
    var id: Int,
    var name: String,
    var contractDay: LocalDate,
    var department: Department = Department(1, "D"),
    var supervisor: Employee? = null
)

/** Model of the entity can be a generated class in the feature */
open class Employess : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property("contract_day") { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Model of the entity can be a generated class in the feature */
open class EmployessComposed : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property("contract_day") { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Initialize, register and close the entity model. */
val ModelProvider.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employess().close() as Employess
}