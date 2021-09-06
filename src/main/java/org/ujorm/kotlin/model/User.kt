package org.ujorm.kotlin.model

import org.ujorm.kotlin.*
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
    val invitedFrom = propertyNle({ it.invitedFrom })
}

interface IDomain<D : Any> {
    fun _context(): PropertyFactory<D>
    fun domain(): KClass<D>
}

interface _User2 : IDomain<User> {
    override fun domain() = User::class
    val id get() = _context().property({ it.id })
    val nickname get() = _context().property({ it.nickname })
    val born get() = _context().property({ it.born })
    val department get() = _context().property({ it.department })
    //val invitedFrom get() = container().property({ it.invitedFrom })
}

class PropertyFactory<D : Any>(
    /** Get the main domain class */
    val _entityClass: KClass<D>,
    private var _size: Short = 0
) {
    public fun <V : Any> property(
        getter: (D) -> V,
        setter: (D, V?) -> Unit = Constants.UNDEFINED_SETTER
    ): MandatoryProperty<D, V> = MandatoryPropertyImpl<D, V>(_size++, "", getter, setter, _entityClass)
}


/**
 * Return a default entity sequence of
 */
val ModelProvider.user by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _User().init() as _User }