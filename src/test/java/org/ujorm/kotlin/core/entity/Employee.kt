package org.ujorm.kotlin.core.entity

import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.PropertyAccessor
import org.ujorm.kotlin.core.impl.EntityModel

/** A user entity
 * (with an optional interface to access property values by a property descriptor). */
@Entity
interface Employee : PropertyAccessor<Employee> {
    var id: Int
    var name: String
    var higherEducation: Boolean
    var contractDay: LocalDate
    var department: Department
    var superior: Employee?
}

/** Model of the entity can be a generated class in the feature */
class Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val higherEducation = property { it.higherEducation }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val superior = propertyNullable { it.superior }

    // Optional composed properties:
    val departmentName by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + MyDatabase.departments.name }
    val departmentId by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        department + MyDatabase.departments.id }
}

/** Initialize, register and close the entity model. */
val MyDatabase.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    MyDatabase.add(Employees().close<Employees>())
}