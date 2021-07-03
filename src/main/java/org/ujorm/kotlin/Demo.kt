package org.ujorm.kotlin

import java.time.LocalDate

/** Sample of usage */
fun main(args: Array<String>) {
    val _user = ModelProvier.user
    val user = User(11, "Pavel", LocalDate.now(), null);

    val crn1 = _user.name EQ "Pavel"
    val crn2 = _user.id GT 1
    val crn3 = _user.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)

    val isValid : Boolean = crn4.eval(user)
    val userName : String = _user.name.of(user)
    val id : Int = _user.id.of(user)
    //val parent : String = _user.name.parent.of(user) // TODO

    println("Valid: $isValid, name=$userName, id=$id")
}

/** Domain object */
data class User constructor (
    val id: Int,
    val name: String,
    val born: LocalDate,
    val parent: User?) {
}

/** Meta-model of the domain object */
open class _User {
    val id : Key<User, Int> = KeyImpl("id", User::class, Int::class)
    val name : Key<User, String> = KeyImpl("name", User::class, String::class)
    val born : Key<User, LocalDate> = KeyImpl("name", User::class, LocalDate::class)
    val parent : Key<User, User> = KeyImpl("name", User::class, User::class)
}

/** Provider of meta-models */
object ModelProvier {
    val user = _User()
}
