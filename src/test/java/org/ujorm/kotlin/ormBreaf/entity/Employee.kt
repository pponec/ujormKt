package org.ujorm.kotlin.ormBreaf.entity

import org.ujorm.kotlin.core.EntityModel
import java.time.LocalDate

/** An user entity */
interface Employee {
    var id: Int
    var name: String
    var contractDay: LocalDate
    var department: Department
    var supervisor: Employee?
}

/** Model of the entity can be a generated class in the feature */
open class Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }

    // Optional composed properties:
    val departmentName by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + Database.departments.name }
    val departmentId by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + Database.departments.id }
}


/** Initialize, register and close the entity model. */
val Database.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees().close<Employees>()
}