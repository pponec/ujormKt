package org.ujorm.kotlin.model

import org.ujorm.kotlin.EntityModel
import org.ujorm.kotlin.ModelProvider
import java.time.LocalDate

/** An user entity */
data class User constructor(
    var id: Int,
    var nickname: String,
    var born: LocalDate,
    var parent: User? = null
)

/** Model of the entity can be a generated class in the feature */
open class _User : EntityModel<User>(User::class) {
    val id = property({ it.id })
    val nickname = property({ it.nickname })
    val born = property({ it.born })
    val parent = propertyN6e({ it.parent })
}

/**
 * Return a default entity sequence of
 */
val ModelProvider.user by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _User().init() as _User }