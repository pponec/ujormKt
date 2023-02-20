package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.coreComposed.DomainEntityModel
import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.impl.EntityModel

/** A user entity */
@Entity
interface Employee {
    var id: Int
    var name: String
    var senior: Boolean
    var contractDay: LocalDate
    var department: Department
    var supervisor: Employee?
}

/** Model of the entity can be a generated class in the feature */
open class _Employees : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val senior = property { it.senior }
    val contractDay = property { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Model of the entity can be a generated class in the feature */
open class Employees<D : Any>() : DomainEntityModel<D, Employee>() {
    override val core: _Employees = _Employees().close()

    val id get() = propertyModel(core.id)
    val name get() = propertyModel(core.name)
    val senior get() = propertyModel(core.senior)
    val contractDay get() = propertyModel(core.contractDay)
    val department: Departments<D> get() = propertyModel(core.department)
    val supervisor: Employees<D> get() = propertyModel(core.supervisor)
}

/** Initialize, register and close the entity model. */
val Database.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees<Employee>().close() as Employees<Employee>
}