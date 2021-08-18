package org.ujorm.kotlin.model

import org.ujorm.kotlin.EntityModel
import org.ujorm.kotlin.ModelProvider
import java.time.LocalDate

/** An user entity */
interface User {
    var id: Int;
    var nickname: String;
    var born: LocalDate
    var parent: User?
}

/** Model of the entity can be a generated class in the feature */
open class _User : EntityModel<User>(User::class) {
    val id = property({ it.id }, {0})
    val nickname = property({ it.nickname }, {""})
    val born = property({ it.born }, {LocalDate.of(0,1,1)})
    val parent = propertyNle({ it.parent })
}

/**
 * Return a default entity sequence of
 */
val ModelProvider.user by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _User().init() as _User }