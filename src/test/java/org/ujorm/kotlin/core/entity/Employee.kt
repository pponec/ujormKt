package org.ujorm.kotlin.core.entity

import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.PropertyAccessor
import org.ujorm.kotlin.core.impl.EntityModel

/** An user entity
 * (with an optional interface to access property values by a property descriptor). */
@Entity
interface Employee : PropertyAccessor<Employee> {
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