package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.impl.EntityModel
import java.time.LocalDate

/** A user entity */
@Entity
interface Employee {
    var id: Int
    var name: String
    var higherEducation: Boolean
    var contractDay: LocalDate
    var department: Department
    var superior: Employee?
}

/** Model of the entity can be a generated class in the feature */
open class Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val higherEducation = property { it.higherEducation }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val superior = propertyNullable { it.superior }
}

/** Initialize, register and close the entity model. */
val MyDatabase.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    MyDatabase.add(Employees().close<Employees>())
}