package org.ujorm.kotlin.model

import org.ujorm.kotlin.*
import org.ujorm.kotlin.model.provider.ModelProvider
import java.time.LocalDate
import kotlin.reflect.KClass

/** An user entity */
data class User constructor(
    var id: Int,
    var nickname: String,
    var born: LocalDate,
    var department: Department = Department(1, "A"),
    var invitedFrom: User? = null
)

/** Model of the entity can be a generated class in the feature */
open class _User : EntityModel<User>(User::class) {
    val id = property({ it.id })
    val nickname = property({ it.nickname })
    val born = property({ it.born })
    val department = property({ it.department })
    val invitedFrom = propertyNullable({ it.invitedFrom })
}

/**
 * Return a default entity sequence of
 */
val ModelProvider.user by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _User().init() as _User }