package org.ujorm.kotlin.coreComposed.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.PropertyAccessor
import org.ujorm.kotlin.core.impl.EntityModel
import org.ujorm.kotlin.coreComposed.DomainEntityModel
import java.time.LocalDate

/** A user entity with reading value by properties */
@Entity
interface Employee: PropertyAccessor<Employee> {
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

    /** Attribute */
    val id get() = property(core.id)
    /** Attribute */
    val name get() = property(core.name)
    /** Attribute */
    val contractDay get() = propertyModel(core.contractDay)
    /** Relation */
    val department: Departments<D> get() = propertyModel(core.department)
    /** Relation */
    val supervisor: Employees<D> get() = propertyModel(core.supervisor)
}

/** Initialize, register and close the entity model. */
val Entities.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees<Employee>() //.close() as Employees<Employee>
}