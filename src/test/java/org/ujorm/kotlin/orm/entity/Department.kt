package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.anotation.OneToMany
import org.ujorm.kotlin.core.impl.EntityModel
import java.time.LocalDate

/** A Department entity */
@Entity
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
    @OneToMany(criterion = SeniorEmployeeFilter::class)
    var members: List<Employee>
}

/** Model of the entity can be a generated class in the feature */
open class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { Department::created } // Alternative notation
    val members = propertyList { Employee::department } // Notation for a Relation 1:M
}

/** Initialize, register and close the entity model. */
val MyDatabase.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    MyDatabase.add(Departments().close<Departments>())
}