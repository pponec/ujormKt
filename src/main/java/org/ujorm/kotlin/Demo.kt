/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.kotlin.demo

import org.ujorm.kotlin.*
import java.time.LocalDate
import kotlin.reflect.KClass

/** Sample of usage */
fun main() {
    val _user = ModelProvider.user
    val crn1 = _user.name EQ "Pavel"
    val crn2 = _user.id GT 1
    val crn3 = _user.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)
    val crn5 = crn1.not() OR (crn2 AND crn3)
    assert(crn1.toString() == "User: name EQ \"Pavel\"")
    assert(crn2.toString() == "User: id GT 1")
    assert(crn4.toString() == "User: (name EQ \"Pavel\") OR ((id GT 1) AND (id LT 99))")
    assert(crn5.toString() == "User: (NOT (name EQ \"Pavel\")) OR ((id GT 1) AND (id LT 99))")

    val user = User(id = 11, name = "Xaver", born = LocalDate.now())
    val noValid : Boolean = crn1.eval(user)
    val isValid : Boolean = crn4.eval(user)
    val userName : String = _user.name.of(user)
    val userId : Int = _user.id.of(user)
    val parent : User? = _user.parent.of(user)
    //val parentName : String = _user.name.parent.of(user) // TODO: reading the relations
    assert(!noValid, { "crn1.eval(user)" })
    assert( isValid, { "crn4.eval(user)" })
    assert(userName == "Xaver", { "userName" })
    assert(userId == 11, { "userId" })
    assert(parent == null, { "userId" })

    _user.name.set(user, "James")
    _user.parent.set(user, null)

    val nameId1 : String = _user.id.toString()
    val nameId2 : String = _user.id()
    assert(nameId1 == "id", { "nameId1" } )
    assert(nameId2 == "id", { "nameId2" } )
}

/** Domain object */
data class User constructor (
    var id: Int,
    var name: String,
    var born: LocalDate,
    var parent: User? = null)

/** Meta-model of the domain object will be a generated class in the feature */
open class _User : DomainModel{
    override val _domainClass: KClass<User> get() = User::class
    val id : Key<User, Int> = KeyImpl("id",
        domainClass = _domainClass,
        valueClass = Int::class,
        setter = { d : User, v : Int? -> d.id = v!! },
        getter = { d : User -> d.id })
    val name : Key<User, String> = KeyImpl("name",
        domainClass = _domainClass,
        valueClass = String::class,
        setter = { d : User, v : String? -> d.name = v!! },
        getter = { d : User -> d.name })
    val born : Key<User, LocalDate> = KeyImpl("born",
        domainClass = _domainClass,
        valueClass = LocalDate::class,
        setter = { d : User, v : LocalDate? -> d.born = v!! },
        getter = { d : User -> d.born })
    val parent : KeyNullable<User, User> = KeyNullableImpl("parent",
        domainClass =_domainClass,
        valueClass = User::class,
        setter = { d : User, v : User? -> d.parent = v },
        getter = { d : User -> d.parent}) // TODO: how to return the nullable value?

    override val attributes: List<KeyNullable<User, Any>>
        get() = TODO("listOf(id, name, parent), ...")
}

/** Provider of meta-models */
object ModelProvider : AbstractModelProvider {
    val user = _User()
    override val entityModels: List<DomainModel>
        get() = TODO("listOf(user)")
}
