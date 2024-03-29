package org.ujorm.kotlin.ormBreaf.entity

import org.ujorm.kotlin.core.impl.EntityModel
import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity

/** A user entity */
@Entity
interface Employee {
    var id: Int
    var name: String
    var contractDay: LocalDate
    var department: Department
    var superior: Employee?
}

/** Model of the entity can be a generated class in the feature */
open class Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val superior = propertyNullable { it.superior }

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