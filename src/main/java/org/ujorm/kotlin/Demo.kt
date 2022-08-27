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
package org.ujorm.kotlin

import org.ujorm.kotlin.model.Employee
import org.ujorm.kotlin.config.ModelProvider
import org.ujorm.kotlin.model.Department
import org.ujorm.kotlin.model.employees
import java.time.LocalDate

/** Sample of usage */
fun main() {
    basicSkills()
    criterions()
    moreInfo()
    entityBuilder()
}

/** Sample of usage */
fun basicSkills() {
    val employee = Employee(id = 11, name = "John", contractDay = LocalDate.now())
    val employees = ModelProvider.employees // Entity meta-model

    // Read and Write values by entity meta-model:
    val id = employees.id[employee]
    val name = employees.name[employee]
    val contractDay = employees.contractDay[employee]
    val department = employees.department[employee]
    val supervisor = employees.supervisor[employee]
    employees.id[employee] = id
    employees.name[employee] = name
    employees.contractDay[employee] = contractDay
    employees.department[employee] = department
    employees.supervisor[employee] = supervisor

    // Relations with composed properties: (TODO)
    //val employeeDepartmentName : Property<Employee, String> = employees.department.name;
    //val departmentName : String = employeeDepartmentName[employee]
    //employeeDepartmentName[employee] = departmentName
}

/** Sample of usage */
fun criterions() {
    val employee = Employee(id = 11, name = "John", contractDay = LocalDate.now())
    val employees = ModelProvider.employees // MetaModel

    // Criterion conditions:
    val crn1 = employees.name EQ "Lucy"
    val crn2 = employees.id GT 1
    val crn3 = employees.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)
    val crn5 = crn1.not() OR (crn2 AND crn3)
    val noValid: Boolean = crn1(employee)
    val isValid: Boolean = crn4(employee)

    // Criterion logs:
    assert(!noValid, { "crn1(employee)" })
    assert(isValid, { "crn4(employee)" })
    assert(crn1.toString() == """Employee: name EQ "Lucy"""")
    assert(crn2.toString() == """Employee: id GT 1""")
    assert(crn4.toString() == """Employee: (name EQ "Lucy") OR (id GT 1) AND (id LT 99)""")
    assert(crn5.toString() == """Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (id LT 99)""")
}

/** Sample of usage */
fun moreInfo() {
    val employee = Employee(id = 11, name = "John", contractDay = LocalDate.now())
    val employees = ModelProvider.employees

    val id: Int = employees.id[employee]
    val name: String = employees.name[employee] // Get a name of the employee
    val supervisor: Employee? = employees.supervisor[employee]

    assert(name == "John", { "employee name" })
    assert(id == 11, { "employee id" })
    assert(supervisor == null, { "employee supervisor " })

    employees.name[employee] = "James" // Set a name to the user
    employees.supervisor[employee] = null
    assert(employees.id.name == "id") { "property name" }
    assert(employees.id.toString() == "id") { "property name" }
    assert(employees.id.info() == "Employee.id") { "property name" }
    assert(employees.id() == "Employee.id") { "property name" }

    val properties = ModelProvider.employees._properties
    assert(properties.size == 5) { "Count of properties" }
    assert(properties[0].name == "id") { "property name" }
    assert(properties[1].name == "name") { "property name" }
    assert(properties[2].name == "contract_day") { "property name" } // User defined name
    assert(properties[3].name == "department") { "property name" }
    assert(properties[4].name == "supervisor") { "property name" }

    // Value type
    assert(employees.id.valueClass == Int::class)
    assert(employees.contractDay.valueClass == LocalDate::class)

    // Entity type (alias domain type)
    assert(employees.id.entityClass == Employee::class)
    assert(employees.contractDay.entityClass == Employee::class)
}

/** Create new object by a constructor (for immutable objects) */
fun entityBuilder() {
    val employees = ModelProvider.employees
    val employee: Employee = employees.builder()
        .set(employees.id, 1)
        .set(employees.name, "John")
        .set(employees.contractDay, LocalDate.now())
        .set(employees.department, Department(2, "B"))
        .set(employees.supervisor, null) // Supervisor is optional
        .build()

    assert(employee.id == 1)
    assert(employee.name == "John")
}
