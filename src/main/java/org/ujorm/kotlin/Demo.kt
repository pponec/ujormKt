package org.ujorm.kotlin

import java.time.LocalDate

/** Sample of usage */
fun main(args: Array<String>) {
    val _user = ModelProvier.user
    val user = User(11, "Pavel", LocalDate.now(), null)

    val crn1 = _user.name EQ "Pavel"
    val crn2 = _user.id GT 1
    val crn3 = _user.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)
    //val crn5 = crn1.not() OR (crn2 AND crn3) // TODO

    val isValid : Boolean = crn4.eval(user)
    val userName : String = _user.name.of(user)
    val id : Int = _user.id.of(user)
    //val parent : String = _user.name.parent.of(user) // TODO

    println("Valid: $isValid, name=$userName, id=$id")
}

/** Domain object */
data class User constructor (
    var id: Int,
    var name: String,
    var born: LocalDate,
    var parent: User?)

/** Meta-model of the domain object will be a generated class in the feature */
open class _User {
    val id : Key<User, Int> = KeyImpl("id",
        domainClass = User::class,
        valueClass = Int::class,
        setter = { d : User, v : Int -> d.id = v },
        getter = { d : User -> d.id })
    val name : Key<User, String> = KeyImpl("name",
        domainClass = User::class,
        valueClass = String::class,
        setter = { d : User, v : String -> d.name = v },
        getter = { d : User -> d.name })
    val born : Key<User, LocalDate> = KeyImpl("name",
        domainClass = User::class,
        valueClass = LocalDate::class,
        setter = { d : User, v : LocalDate -> d.born = v },
        getter = { d : User -> d.born })
    val parent : Key<User, User> = KeyImpl("name",
        domainClass = User::class,
        valueClass = User::class,
        setter = { d : User, v : User -> d.parent = v },
        getter = { d : User -> d.parent })
}

/** Provider of meta-models */
object ModelProvier : AbstractMetaModel {
    val user = _User()
}
