package org.ujorm.kotlin.ormBreaf.entity

import org.ujorm.kotlin.anotation.Entity
import org.ujorm.kotlin.core.EntityModel
import java.time.LocalDate

/** A Department entity */
@Entity
interface Department {
    var id: Int
    var name: String
    var created: LocalDate
}

/** Model of the entity can be a generated class in the feature */
open class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}

/** Initialize, register and close the entity model. */
val Database.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments().close<Departments>()
}