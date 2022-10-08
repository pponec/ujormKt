package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.*
import java.time.LocalDate

/** An user entity */
interface Employee : Entity<Employee> {
    var id: Int
    var name: String
    var contractDay: LocalDate
    var department: Department
    var supervisor: Employee?
}

/** Model of the entity can be a generated class in the feature */
class Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property(/*"contract_day"*/) { it.contractDay } // TODO()
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }

    // Optional composed properties:
    val departmentName by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + Entities.departments.name }
    val departmentId by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + Entities.departments.id }
}

/** Initialize, register and close the entity model. */
val Entities.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Entities.add(Employees().close<Employees>())
}