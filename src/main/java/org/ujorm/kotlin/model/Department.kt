package org.ujorm.kotlin.model

import org.ujorm.kotlin.EntityModel
import org.ujorm.kotlin.ModelProvider
import java.time.LocalDate

/** An Department entity */
data class Department constructor(
    var id: Int,
    var name: String,
    var created: LocalDate = LocalDate.now().minusDays(1),
)

/** Model of the entity can be a generated class in the feature */
open class _Department : EntityModel<Department>(Department::class) {
    val id = property({ it.id })
    val name = property({ it.name })
    val created = property({ it.created })
}

/** Return a default entity sequence of */
val ModelProvider.Department by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _Department().init() as _Department }