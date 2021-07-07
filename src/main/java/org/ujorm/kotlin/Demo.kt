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
    val _user = EntityModelProvider.user
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

    assert("id" == _user.id.toString(), { "id" } )
    assert("id" == _user.id(), { "id" } )
}

/** An entity */
data class User constructor (
    var id: Int,
    var name: String,
    var born: LocalDate,
    var parent: User? = null)

/** Model of the entity will be a generated class in the feature */
open class _User : EntityModel{
    override val _entityClass: KClass<User> get() = User::class
    val id : Property<User, Int> = PropertyImpl("id",
        entityClass = _entityClass,
        valueClass = Int::class,
        setter = { d : User, v : Int? -> d.id = v!! },
        getter = { d : User -> d.id })
    val name : Property<User, String> = PropertyImpl("name",
        entityClass = _entityClass,
        valueClass = String::class,
        setter = { d : User, v : String? -> d.name = v!! },
        getter = { d : User -> d.name })
    val born : Property<User, LocalDate> = PropertyImpl("born",
        entityClass = _entityClass,
        valueClass = LocalDate::class,
        setter = { d : User, v : LocalDate? -> d.born = v!! },
        getter = { d : User -> d.born })
    val parent : PropertyNullable<User, User> = PropertyNullableImpl("parent",
        entityClass =_entityClass,
        valueClass = User::class,
        setter = { d : User, v : User? -> d.parent = v },
        getter = { d : User -> d.parent}) // TODO: how to return the nullable value?

    override val _properties: List<PropertyNullable<User, Any>>
        get() = TODO("listOf(id, name, parent), ...")
}

/** Model provider of entity */
object EntityModelProvider : AbstractModelProvider {
    val user = _User()
    override val entityModels: List<EntityModel>
        get() = TODO("listOf(user)")
}
