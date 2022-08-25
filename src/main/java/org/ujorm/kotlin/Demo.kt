/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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

import org.ujorm.kotlin.Property
import org.ujorm.kotlin.ValueCriterion
import org.ujorm.kotlin.model.Department
import org.ujorm.kotlin.model.User
import org.ujorm.kotlin.model.provider.ModelProvider
import org.ujorm.kotlin.model.user
import java.time.LocalDate

/** Sample of usage */
fun main() {
    useCriterions()
    useProperties()
    useEntityBuilder()
}

/** Sample of usage */
fun useCriterions() {
    val _user = ModelProvider.user

    val userId: Property<User, Int> = _user.id
    val crn0: ValueCriterion<User, Int> = userId EQ 123
    assert(crn0.toString() == "User: id EQ 123")

    val crn1 = _user.nickname EQ "Pavel"
    val crn2 = _user.id GT 1
    val crn3 = _user.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)
    val crn5 = crn1.not() OR (crn2 AND crn3)
    assert(crn1.toString() == """User: nickname EQ "Pavel"""")
    assert(crn2.toString() == """User: id GT 1""")
    assert(crn4.toString() == """User: (nickname EQ "Pavel") OR (id GT 1) AND (id LT 99)""")
    assert(crn5.toString() == """User: (NOT (nickname EQ "Pavel")) OR (id GT 1) AND (id LT 99)""")

    val user = User(id = 11, nickname = "Xaver", born = LocalDate.now())
    val noValid: Boolean = crn1(user)
    val isValid: Boolean = crn4(user)
    assert(!noValid, { "crn1(user)" })
    assert(isValid, { "crn4(user)" })
}

/** Sample of usage */
fun useProperties() {
    val _user = ModelProvider.user
    val user = User(id = 11, nickname = "Xaver", born = LocalDate.now())

    val userName: String = _user.nickname[user] // Get a name of the user
    val userId: Int = _user.id[user]
    val parent: User? = _user.invitedFrom[user]

    // TODO(ponec): read an attribute of the relation:
    //val userDepartmentName : MandatoryProperty<User, String> = _user.department().name;
    //val departmentName : String = userDepartmentName(user)

    assert(userName == "Xaver", { "userName" })
    assert(userId == 11, { "userId" })
    assert(parent == null, { "userId" })

    _user.nickname[user] = "James" // Set a name to the user
    _user.invitedFrom[user] = null
    assert(_user.id.name == "id") { "property name" }
    assert(_user.id.toString() == "id") { "property name" }
    assert(_user.id.info() == "User.id") { "property name" }
    assert(_user.id() == "User.id") { "property name" }

    val properties = ModelProvider.user._properties
    assert(properties.size == 5) { "Count of properties" }
    assert(properties[0].name == "id") { "property name" }
    assert(properties[1].name == "nickname") { "property name" }
    assert(properties[2].name == "born") { "property name" }

    // Value type
    assert(_user.id.valueClass == Int::class)
    assert(_user.born.valueClass == LocalDate::class)

    // Entity type (alias domain type)
    assert(_user.id.entityClass == User::class)
    assert(_user.born.entityClass == User::class)
}

/** Create new object by a constructor (for immutable objects) */
fun useEntityBuilder() {
    val _user = ModelProvider.user
    val user: User = _user.builder()
        .set(_user.id, 1)
        .set(_user.nickname, "John")
        //.set(_user.name, null) // Compilator fails
        .set(_user.born, LocalDate.now())
        .set(_user.department, Department(2, "B"))
        .set(_user.invitedFrom, null)
        .build()

    assert(user.id == 1)
    assert(user.nickname == "John")
}
