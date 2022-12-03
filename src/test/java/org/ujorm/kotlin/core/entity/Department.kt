package org.ujorm.kotlin.core.entity

import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.EntityInitializer
import org.ujorm.kotlin.core.impl.EntityModel

/** A Department entity */
@Entity(init = DepartmentInitializer::class )
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
    var members: List<Employee>
}

/** Model of the entity can be a generated class in the feature */
class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { Department::created } // Alternative notation
    val members = propertyList { Employee::department } // Notation for a Relation 1:M
}

class DepartmentInitializer : EntityInitializer<Department> {
    override fun initialize(entity: Department) {
        entity.created = LocalDate.now()
    }
}

/** Initialize, register and close the entity model. */
val Entities.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Entities.add(Departments().close<Departments>())
}