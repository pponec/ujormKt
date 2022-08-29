package org.ujorm.kotlin.model.composed

import org.ujorm.kotlin.EntityModel
import org.ujorm.kotlin.config.ModelProvider
import java.time.LocalDate

/** An Department entity */
data class Department constructor(
    var id: Int,
    var name: String,
    var created: LocalDate = LocalDate.now().minusDays(1),
)

/** Model of the entity can be a generated class in the feature */
open class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}

/** Initialize, register and close the entity model. */
val ModelProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments().close() as Departments
}