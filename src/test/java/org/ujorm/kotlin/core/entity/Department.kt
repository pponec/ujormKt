package org.ujorm.kotlin.core.entity

import org.ujorm.kotlin.core.*
import java.time.LocalDate

/** An Department entity */
interface Department : Entity<Department> {
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
val EntityProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    EntityProvider.add(Departments().close<Departments>())
}