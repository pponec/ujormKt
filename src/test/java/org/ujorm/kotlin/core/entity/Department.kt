package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.*
import java.time.LocalDate
import org.ujorm.kotlin.anotation.Entity

/** A Department entity */
@Entity
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
}

/** Model of the entity can be a generated class in the feature */
class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}

/** Initialize, register and close the entity model. */
val Entities.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Entities.add(Departments().close<Departments>())
}