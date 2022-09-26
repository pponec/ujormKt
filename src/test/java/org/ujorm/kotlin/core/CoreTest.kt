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
package org.ujorm.kotlin.core

import org.junit.jupiter.api.Test
import org.ujorm.kotlin.core.entity.*
import java.time.LocalDate
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*

internal class CoreTest {

    @Test
    fun createEntities() {
        // Get the metamodel:
        val employees: Employees = ModelProvider.employees
        val departments: Departments = ModelProvider.departments

        // Create some entities:
        val development: Department = departments.new().apply {
            id = 1
            name = "development"
            created = LocalDate.of(2021, 10, 15)
        }
        val lucy: Employee = employees.new().apply {
            id = 2
            name = "Lucy"
            contractDay = LocalDate.of(2022, 1, 1)
            supervisor = null
            department = development
        }
        val joe: Employee = employees.new().apply {
            id = 3
            name = "Joe"
            contractDay = LocalDate.of(2022, 2, 1)
            supervisor = lucy
            department = development
        }

        expect(joe.name).toEqual("Joe")
        expect(joe.department.name).toEqual("development")
        expect(joe.supervisor?.name).toEqual("Lucy")
        expect(joe.supervisor?.department?.name).toEqual("development")

        expect(development.toString()).toEqual("Department{id=1" +
                ", name=development" +
                ", created=2021-10-15}")
        expect(lucy.toString()).toEqual("Employee{id=2" +
                ", name=Lucy" +
                ", contractDay=2022-01-01" +
                ", department=Department{id=1, name=development, c...}" +
                ", supervisor=null}")
        expect(joe.toString()).toEqual("Employee{id=3" +
                ", name=Joe" +
                ", contractDay=2022-02-01" +
                ", department=Department{id=1, name=development, c...}" +
                ", supervisor=Employee{id=2, name=Lucy, contractDa...}}")
    }

    /** Test writing and reading to the object using the metamodel. */
    @Test
    fun readAndWrite() {
        val employees = ModelProvider.employees // Employee metamodel
        val departments = ModelProvider.departments // Department metamodel

        val employee = employees.new().apply {// New employee object
            id = 11
            name = "John"
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }

        // Read and Write values by entity metamodel:
        val id: Int = employees.id[employee]
        val name: String = employees.name[employee]
        val contractDay: LocalDate = employees.contractDay[employee]
        val department: Department = employees.department[employee]
        val supervisor: Employee? = employees.supervisor[employee]
        employees.id[employee] = id
        employees.name[employee] = name
        employees.contractDay[employee] = contractDay
        employees.department[employee] = department
        employees.supervisor[employee] = supervisor

        // Composed properties:
        val employeeDepartmentId = (employees.department + departments.id)[employee]
        val employeeDepartmentName = (employees.department + departments.name)[employee]
        expect(employeeDepartmentId).toEqual(2) // "Department id must be 2
        expect(employeeDepartmentName).toEqual("D") // Department name must be 'D'

        // Prepared composed properties.:
        val employeeDepartmentId2 = employees.departmentId[employee]
        val employeeDepartmentName2 = employees.departmentName[employee]
        expect(employeeDepartmentId2).toEqual(2) // Department id must be 2
        expect(employeeDepartmentName2).toEqual("D") // Department name must be 'D'
    }

    /** Test conditions */
    @Test
    fun conditions() {
        val employees = ModelProvider.employees // Employee Entity metamodel
        val departments = ModelProvider.departments // Department Entity metamodel
        val employee = employees.new().apply {// New employee object
            id = 11
            name = "John"
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }

        // Criterion conditions:
        val crn1 = employees.name EQ "Lucy"
        val crn2 = employees.id GT 1
        val crn3 = (employees.department + departments.id) LT 99
        val crn4 = crn1 OR (crn2 AND crn3)
        val crn5 = crn1.not() OR (crn2 AND crn3)
        val noValid: Boolean = crn1(employee)
        val isValid: Boolean = crn4(employee)

        // Criterion logs:
        expect(noValid).toEqual(false) // crn1(employee)
        expect(isValid).toEqual(true)  // crn4(employee)
        expect(crn1.toString()).toEqual("""Employee: name EQ "Lucy"""")
        expect(crn2.toString()).toEqual("""Employee: id GT 1""")
        expect(crn3.toString()).toEqual("""Employee: department.id LT 99""")
        expect(crn4.toString()).toEqual("""Employee: (name EQ "Lucy") OR (id GT 1) AND (department.id LT 99)""")
        expect(crn5.toString()).toEqual("""Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (department.id LT 99)""")
    }

    /** Sample of usage */
    @Test
    fun extendedFunctions() {
        val employees = ModelProvider.employees // Employee Entity metamodel
        val departments = ModelProvider.departments // Department Entity metamodel
        val employee = employees.new().apply {// New employee object
            id = 11
            name = "John"
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }

        val id: Int = employees.id[employee]
        val name: String = employees.name[employee] // Get a name of the employee
        val supervisor: Employee? = employees.supervisor[employee]

        expect(name).toEqual("John") // employee name
        expect(id).toEqual(11) // employee id
        expect(supervisor).toEqual(null) // employee supervisor

        employees.name[employee] = "James" // Set a name to the user
        employees.supervisor[employee] = null
        expect(employees.id.name()).toEqual("id") // property id
        expect(employees.id.toString()).toEqual("Employee.id") // property id
        expect(employees.id.info()).toEqual("Employee.id") // property id
        expect(employees.id()).toEqual("id") // A shortcut for the name()

        val properties = ModelProvider.employees.utils().properties
        expect(properties.size).toEqual(5) // Count of properties
        expect(properties[0].name()).toEqual("id") // property id
        expect(properties[1].name()).toEqual("name") // property name
        expect(properties[2].name()).toEqual("contractDay")// ContractDay
        expect(properties[3].name()).toEqual("department") // property department
        expect(properties[4].name()).toEqual("supervisor") // property supervisor

        // Value type
        expect(employees.id.data().valueClass).toEqual(Int::class)
        expect(employees.contractDay.data().valueClass).toEqual(LocalDate::class)

        // Entity type (alias domain type)
        expect(employees.id.data().entityClass).toEqual(Employee::class)
        expect(employees.contractDay.data().entityClass).toEqual(Employee::class)

        // Composed properties:
        val employeeDepartmentNameProp:
                Property<Employee, String> = employees.department + departments.name
        expect(employeeDepartmentNameProp.info()).toEqual("Employee.department.name")
        expect(employeeDepartmentNameProp.toString()).toEqual("department.name")
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun entityHashAndAlias() {
        val employees: Employees = ModelProvider.employees.alias("e")
        val department: Departments = ModelProvider.departments.alias("d")

        println(employees.id.status())

        expect(employees.id.entityAlias()).toEqual("e")
        expect(employees.name.entityAlias()).toEqual("e")
        expect(employees.department.entityAlias()).toEqual("e")
        expect(department.id.entityAlias()).toEqual("d")
        expect(department.name.entityAlias()).toEqual("d")
        expect(employees.id.info()).toEqual("Employee(e).id")
        expect(employees.department.info()).toEqual("Employee(e).department")

        var idAliasHash = employees.id.hashCode()
        var idHash = ModelProvider.employees.id.hashCode()
        var nameAliasHash = employees.name.hashCode()
        var idAliasName = employees.id.name()
        var idName = ModelProvider.employees.id.name()
        var idAlias2 = ModelProvider.employees.id.entityAlias("e") // Alias for a single property
        var idAlias3 = ModelProvider.employees.id("e") // Shortcut for new alias

        expect(idAliasName).toEqual(idName)
        expect(idAliasHash).notToEqual(idHash) // Different hash codes
        expect(idAliasHash).notToEqual(nameAliasHash) // Different hash codes
        expect(employees.id).notToEqual(ModelProvider.employees.id) // Different properties
        expect(idAliasHash).toEqual(idAlias2.hashCode())
        expect(employees.id).toEqual(idAlias2)
        expect(idAlias2).toEqual(idAlias3)
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun entityHashAndEquals() {
        val department1 = getDepartment(1, "development")
        val department2 = getDepartment(1, "development")
        val department3 = getDepartment(1, "accounting")

        expect(department1.hashCode()).toEqual(department2.hashCode())
        expect(department1.hashCode()).notToEqual(department3.hashCode())
        expect(department2.hashCode()).notToEqual(department3.hashCode())

        expect(department1 is Entity<*>).toEqual(true)
        expect(department1).toEqual(department1)
        expect(department1).toEqual(department2)
        expect(department1).notToEqual(department3)
        expect(department2).notToEqual(department3)
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun createArrayOfEntity() {
        val employees = ModelProvider.employees
        val emplyee: Array<Any?> = employees.createArray()

        val id = 17 // Reference value
        employees.id[emplyee] = id
        val expectedId: Int = employees.id[emplyee]
        expect(id).toEqual(expectedId)

        val name = "John" // Reference value
        employees.name[emplyee] = name
        val expectedName: String = employees.name[emplyee]
        expect(name).toEqual(expectedName)
    }
}

/** Helper method to create new department */
private fun getDepartment(id: Int, name: String) =
    ModelProvider.departments.new().apply {
        this.id = id
        this.name = name
    }