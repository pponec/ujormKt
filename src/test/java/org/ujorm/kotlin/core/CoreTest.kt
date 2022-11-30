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
import org.ujorm.kotlin.core.impl.ComposedPropertyImpl
import org.ujorm.kotlin.core.impl.ComposedPropertyNullableImpl

internal class CoreTest {

    @Test
    fun createEntities() {
        // Get the metamodel(s):
        val employees: Employees = Entities.employees
        val departments: Departments = Entities.departments

        // Create some new entities:
        val development: Department = departments.new {
            id = 1
            name = "development"
            created = LocalDate.of(2021, 10, 15)
        }
        val lucy: Employee = employees.new {
            id = 2
            name = "Lucy"
            senior = true
            contractDay = LocalDate.of(2022, 1, 1)
            supervisor = null
            department = development
        }
        val joe: Employee = employees.new {
            id = 3
            name = "Joe"
            senior = false
            contractDay = LocalDate.of(2022, 2, 1)
            supervisor = lucy
            department = development
        }

        expect(development.id).toEqual(1)
        expect(joe.name).toEqual("Joe")
        expect(joe.senior).toEqual(false)
        expect(joe.department.name).toEqual("development")
        expect(joe.supervisor?.name).toEqual("Lucy")
        expect(joe.supervisor?.department?.name).toEqual("development")

        expect(development.toString()).toEqual("Department{id=1" +
                ", name=\"development\"" +
                ", created=2021-10-15}")
        expect(lucy.toString()).toEqual("Employee{id=2" +
                ", name=\"Lucy\"" +
                ", senior=true" +
                ", contractDay=2022-01-01" +
                ", department=Department{id=1, name=\"development\",...}" +
                ", supervisor=null}")
        expect(joe.toString()).toEqual("Employee{id=3" +
                ", name=\"Joe\"" +
                ", senior=false" +
                ", contractDay=2022-02-01" +
                ", department=Department{id=1, name=\"development\",...}" +
                ", supervisor=Employee{id=2, name=\"Lucy\", senior=t...}}")
    }

    /** Test writing and reading to the object using the metamodel. */
    @Test
    fun readAndWrite() {
        val employees = Entities.employees // Employee metamodel
        val departments = Entities.departments // Department metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            senior = false
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }

        // Read and Write values by entity metamodel:
        val id: Int = employee[employees.id]
        val name: String = employee[employees.name]
        val senior: Boolean = employee[employees.senior]
        val contractDay: LocalDate = employee[employees.contractDay]
        val department: Department = employee[employees.department]
        val supervisor: Employee? = employee[employees.supervisor]
        employee[employees.id] = id
        employee[employees.name] = name
        employee[employees.senior] = senior
        employee[employees.contractDay] = contractDay
        employee[employees.department] = department
        employee[employees.supervisor] = supervisor

        // Composed properties:
        employee[employees.department + departments.id] = 3
        employee[employees.department + departments.name] = "C"
        expect(employee.department.id).toEqual(3)
        expect(employee.department.name).toEqual("C")
        expect(employee[employees.department + departments.id]).toEqual(3)
        expect(employee[employees.department + departments.name]).toEqual("C")

        // Create relation instance(s): // TODO:
        val employee2 = employees.new()
        val departmentNameProperty = employees.department + departments.name
//      employee2[departmentNameProperty] = "Test"
    }

    /** Test conditions */
    @Test
    fun conditions() {
        val employees = Entities.employees // Employee Entity metamodel
        val departments = Entities.departments // Department Entity metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            senior = false
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }

        // Criterion conditions:
        val crn1 = employees.name EQ "Lucy"
        val crn2 = employees.id GT 1
        val crn3 = (employees.department + departments.id) LT 99
        val crn4 = crn1 OR (crn2 AND crn3)
        val crn5 = crn1.not() OR (crn2 AND crn3)

        expect(crn1(employee)).equals(false) // Invalid employee
        expect(crn4(employee)).equals(true)  // Valid employee
        expect(crn1.toString()).toEqual("""Employee: name EQ "Lucy"""")
        expect(crn2.toString()).toEqual("""Employee: id GT 1""")
        expect(crn3.toString()).toEqual("""Employee: department.id LT 99""")
        expect(crn4.toString()).toEqual("""Employee: (name EQ "Lucy") OR (id GT 1) AND (department.id LT 99)""")
        expect(crn5.toString()).toEqual("""Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (department.id LT 99)""")
    }

    /** Sample of usage */
    @Test
    fun extendedFunctions() {
        val employees = Entities.employees // Employee Entity metamodel
        val departments = Entities.departments // Department Entity metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            senior = false
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

        val properties = employees.utils().properties
        expect(properties.size).toEqual(6) // Count of properties
        expect(properties[0].name()).toEqual("id") // property id
        expect(properties[1].name()).toEqual("name") // property name
        expect(properties[2].name()).toEqual("senior") // property name
        expect(properties[3].name()).toEqual("contractDay")// ContractDay
        expect(properties[4].name()).toEqual("department") // property department
        expect(properties[5].name()).toEqual("supervisor") // property supervisor

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

    @org.junit.jupiter.api.Disabled
    @Test
    fun createNewRelationBySpecialSetter() {
        val entities = Entities.close<Entities>()
        val employees = entities.employees
        val departments = entities.departments
        val employee: Employee = employees.new()

        val deparmentIdProperty = (employees.department + departments.id)
                as ComposedPropertyNullableImpl<Employee, *, Int>
        deparmentIdProperty.set(employee, 88, Entities) // Method creates new Department
        employee[employees.department + departments.name] = "Catherine"
        expect(employee[deparmentIdProperty]).toEqual(88)
        expect(employee[employees.department + departments.name]).toEqual("Catherine")

        val deparmentIdOfDSupervisorProperty = (employees.supervisor + employees.department + departments.id)
                as ComposedPropertyNullableImpl<Employee, *, Int>
        deparmentIdOfDSupervisorProperty.set(employee, 99, Entities) // Method creates new Department
        expect(employee[deparmentIdOfDSupervisorProperty]).toEqual(99)
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun entityHashAndAlias() {
        val aliasEmployees: Employees = Entities.employees.alias("e")
        val aliasDepartment: Departments = Entities.departments.alias("d")

        println(aliasEmployees.id.status())

        expect(aliasEmployees.id.entityAlias()).toEqual("e")
        expect(aliasEmployees.name.entityAlias()).toEqual("e")
        expect(aliasEmployees.department.entityAlias()).toEqual("e")
        expect(aliasDepartment.id.entityAlias()).toEqual("d")
        expect(aliasDepartment.name.entityAlias()).toEqual("d")
        expect(aliasEmployees.id.info()).toEqual("Employee(e).id")
        expect(aliasEmployees.department.info()).toEqual("Employee(e).department")

        var idAliasHash = aliasEmployees.id.hashCode()
        var idHash = Entities.employees.id.hashCode()
        var nameAliasHash = aliasEmployees.name.hashCode()
        var idAliasName = aliasEmployees.id.name()
        var idName = aliasEmployees.id.name()
        var idAlias2 = aliasEmployees.id.entityAlias("e") // Alias for a single property
        var idAlias3 = aliasEmployees.id("e") // Shortcut for new alias

        expect(idAliasName).toEqual(idName)
        expect(idAliasHash).notToEqual(idHash) // Different hash codes
        expect(idAliasHash).notToEqual(nameAliasHash) // Different hash codes
        expect(aliasEmployees.id).notToEqual(Entities.employees.id) // Different properties
        expect(idAliasHash).toEqual(idAlias2.hashCode())
        expect(aliasEmployees.id).toEqual(idAlias2)
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

        expect(department1 is AbstractEntity<*>).toEqual(true)
        expect(department1).toEqual(department1)
        expect(department1).toEqual(department2)
        expect(department1).notToEqual(department3)
        expect(department2).notToEqual(department3)
    }
}

/** Helper method to create new department */
private fun getDepartment(id: Int, name: String): Department =
    Entities.departments.new {
        this.id = id
        this.name = name
    }