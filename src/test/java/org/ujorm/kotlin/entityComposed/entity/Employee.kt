package org.ujorm.kotlin.entityComposed.entity

import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.core.PropertyNullable
import org.ujorm.kotlin.model.DomainEntityModel
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
    val contractDay = property("contract_day") { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Model of the entity can be a generated class in the feature */
open class Employees<D : Any>() : DomainEntityModel<Department>(_Departments()) {

    /** Direct property model */
    private val core = _Employees().close() as _Employees

    fun core() = this.core

    /** Build the new Property */
    protected fun <V : Any> property(property : PropertyNullable<*, V>) : PropertyNullable<D, V> {
        return if (super.baseInstance) {
            property as PropertyNullable<D, V>
        } else {
            TODO("Create composed property")
        }
    }

    /** Clone the model for the new domain */
    fun <P : Any> basedOn(prefixedDomain: PropertyNullable<P, D>): Employees<P> {
        return null !!
    }

    // --- Properties ---

    val id get() = property(core.id)
    val name get() = property(core.name)
    val contractDay get() = property(core.contractDay)
    //val department get() = property(core.department) as Departments<D>
    val department : Departments<Employee> get() =
        ModelProvider.departments.basedOn(core.department)
    val supervisor : Employees<Employee> get() =
        ModelProvider.employees.basedOn(core.supervisor)
}

/** Initialize, register and close the entity model. */
val ModelProvider.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Employees<Employee>().close() as Employees<Employee>
}