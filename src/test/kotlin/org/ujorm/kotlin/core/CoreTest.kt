/*
 * Copyright 2021-2023 Pavel Ponec, https://github.com/pponec
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

@Suppress("UnusedEquals")
internal class CoreTest {

    @Test
    fun createEntities() {
        // Get the metamodel(s):
        val employees: Employees = MyDatabase.employees
        val departments: Departments = MyDatabase.departments

        // Create some new entities:
        val development: Department = departments.new {
            id = 1
            name = "development"
            created = LocalDate.of(2021, 10, 15)
        }
        val lucy: Employee = employees.new {
            id = 2
            name = "Lucy"
            higherEducation = true
            contractDay = LocalDate.of(2022, 1, 1)
            superior = null
            department = development
        }
        val joe: Employee = employees.new {
            id = 3
            name = "Joe"
            higherEducation = false
            contractDay = LocalDate.of(2022, 2, 1)
            superior = lucy
            department = development
        }

        expect(development.id).toEqual(1)
        expect(joe.name).toEqual("Joe")
        expect(joe.higherEducation).toEqual(false)
        expect(joe.department.name).toEqual("development")
        expect(joe.superior?.name).toEqual("Lucy")
        expect(joe.superior?.department?.name).toEqual("development")

        expect(development.toString()).toEqual("Department{id=1" +
                ", name=\"development\"" +
                ", created=2021-10-15" +
                ", members=?}")
        expect(lucy.toString()).toEqual("Employee{id=2" +
                ", name=\"Lucy\"" +
                ", higherEducation=true" +
                ", contractDay=2022-01-01" +
                ", department=Department{id=1, name=\"development\",...}" +
                ", superior=null}")
        expect(joe.toString()).toEqual("Employee{id=3" +
                ", name=\"Joe\"" +
                ", higherEducation=false" +
                ", contractDay=2022-02-01" +
                ", department=Department{id=1, name=\"development\",...}" +
                ", superior=Employee{id=2, name=\"Lucy\", higherEd...}}")
    }

    /** Test writing and reading to the object using the metamodel. */
    @Test
    fun readAndWrite() {
        val employees = MyDatabase.employees // Employee metamodel
        val departments = MyDatabase.departments // Department metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        // Read values by property descriptors:
        val id: Int = employee[employees.id]
        val name: String = employee[employees.name]
        val higherEducation: Boolean = employee[employees.higherEducation]
        val contractDay: LocalDate = employee[employees.contractDay]
        val department: Department = employee[employees.department]
        val superior: Employee? = employee[employees.superior]
        val departmentName: String = employee[employees.department + departments.name]

        // Write values by property descriptors:
        employee[employees.id] = id
        employee[employees.name] = name
        employee[employees.higherEducation] = higherEducation
        employee[employees.contractDay] = contractDay
        employee[employees.department] = department
        employee[employees.superior] = superior
        employee[employees.department + departments.name] = departmentName

        // More composed properties:
        employee[employees.department + departments.id] = 3
        employee[employees.department + departments.name] = "C"
        expect(employee.department.id).toEqual(3)
        expect(employee.department.name).toEqual("C")
        expect(employee[employees.department + departments.id]).toEqual(3)
        expect(employee[employees.department + departments.name]).toEqual("C")
        employee[employees.department + departments.name] = departmentName

        // Set value including all related entities:
        employee.superior = null // reset a superior
        val departmentNameProperty = employees.department + departments.name
        MyDatabase.utils().setValueWithRelations(employee, "X", departmentNameProperty)
        expect(employee.department.name).to("X")

        // Find metamodel by the entity class
        val employesModel = MyDatabase.utils().findEntityModel(Employee::class)
        expect(employesModel).toBeTheInstance(MyDatabase.employees)
    }

    /** Test conditions */
    @Test
    fun conditions() {
        val employees = MyDatabase.employees // Employee Entity metamodel
        val departments = MyDatabase.departments // Department Entity metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        // Criterion conditions:
        val crn1 = employees.name EQ "Lucy"
        val crn2 = employees.id GT 1
        val crn3 = (employees.department + departments.id) LT 99
        val crn4 = crn1 OR (crn2 AND crn3)
        val crn5 = crn1.not() OR (crn2 AND crn3)

        expect(crn4(employee)).equals(true)  // Valid employee
        expect(crn5(employee)).equals(false) // Invalid employee
        expect(crn1.toString())
            .toEqual("""Employee: name EQ "Lucy"""")
        expect(crn2.toString())
            .toEqual("""Employee: id GT 1""")
        expect(crn3.toString())
            .toEqual("""Employee: department.id LT 99""")
        expect(crn4.toString())
            .toEqual("""Employee: (name EQ "Lucy") OR (id GT 1) AND (department.id LT 99)""")
        expect(crn5.toString())
            .toEqual("""Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (department.id LT 99)""")
    }

    /** Sample of usage */
    @Test
    fun extendedFunctions() {
        val employees = MyDatabase.employees // Employee Entity metamodel
        val departments = MyDatabase.departments // Department Entity metamodel
        val employee: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        val id: Int = employees.id[employee]
        val name: String = employees.name[employee] // Get a name of the employee
        val superior: Employee? = employees.superior[employee]

        expect(name).toEqual("John") // employee name
        expect(id).toEqual(11) // employee id
        expect(superior).toEqual(null) // employee superior

        employees.name[employee] = "James" // Set a name to the user
        employees.superior[employee] = null
        expect(employees.id.name()).toEqual("id") // property id
        expect(employees.id.toString()).toEqual("Employee.id") // property id
        expect(employees.id.info()).toEqual("Employee.id") // property id
        expect(employees.id()).toEqual("id") // A shortcut for the name()

        val properties = employees.utils().properties
        expect(properties.size).toEqual(6) // Count of properties
        expect(properties[0].name()).toEqual("id") // property id
        expect(properties[1].name()).toEqual("name") // property name
        expect(properties[2].name()).toEqual("higherEducation") // property name
        expect(properties[3].name()).toEqual("contractDay")// ContractDay
        expect(properties[4].name()).toEqual("department") // property department
        expect(properties[5].name()).toEqual("superior") // property superior

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

    /** Test method setValueWithRelations() */
    @Test
    fun createNewRelationsByUtilsSetter() {
        val entities = MyDatabase.close<MyDatabase>()
        val employees = entities.employees
        val departments = entities.departments
        val employee: Employee = employees.new()

        // Method creates new  Department entity:
        entities.utils().setValueWithRelations(employee, 88,
            employees.department + departments.id)
        expect(employee[employees.department + departments.id]).toEqual(88)

        entities.utils().setValueWithRelations(employee, "Advertising",
            employees.department + departments.name)
        expect(employee[employees.department + departments.name]).toEqual("Advertising")

        // Method creates new Department entity:
        entities.utils().setValueWithRelations(employee, 99,
            employees.superior + employees.department + departments.id)
        expect(employee[employees.superior + employees.department + departments.id]).toEqual(99)
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun entityHashAndAlias() {
        val aliasEmployees: Employees = MyDatabase.employees.alias("e")
        val aliasDepartment: Departments = MyDatabase.departments.alias("d")

        println(aliasEmployees.id.status())

        expect(aliasEmployees.id.entityAlias()).toEqual("e")
        expect(aliasEmployees.name.entityAlias()).toEqual("e")
        expect(aliasEmployees.department.entityAlias()).toEqual("e")
        expect(aliasDepartment.id.entityAlias()).toEqual("d")
        expect(aliasDepartment.name.entityAlias()).toEqual("d")
        expect(aliasEmployees.id.info()).toEqual("Employee(e).id")
        expect(aliasEmployees.department.info()).toEqual("Employee(e).department")

        val idAliasHash = aliasEmployees.id.hashCode()
        val idHash = MyDatabase.employees.id.hashCode()
        val nameAliasHash = aliasEmployees.name.hashCode()
        val idAliasName = aliasEmployees.id.name()
        val idName = aliasEmployees.id.name()
        val idAlias2 = aliasEmployees.id.entityAlias("e") // Alias for a single property
        val idAlias3 = aliasEmployees.id("e") // Shortcut for new alias

        expect(idAliasName).toEqual(idName)
        expect(idAliasHash).notToEqual(idHash) // Different hash codes
        expect(idAliasHash).notToEqual(nameAliasHash) // Different hash codes
        expect(aliasEmployees.id).notToEqual(MyDatabase.employees.id) // Different properties
        expect(idAliasHash).toEqual(idAlias2.hashCode())
        expect(aliasEmployees.id).toEqual(idAlias2)
        expect(idAlias2).toEqual(idAlias3)
    }

    /** Create new object by a constructor (for immutable objects) */
    @Test
    fun entityHashAndEquals() {
        val department1 = createDepartment(1, "development")
        val department2 = createDepartment(1, "development")
        val department3 = createDepartment(1, "accounting")

        expect(department1.hashCode()).toEqual(department2.hashCode())
        expect(department1.hashCode()).notToEqual(department3.hashCode())
        expect(department2.hashCode()).notToEqual(department3.hashCode())

        expect(department1 is AbstractEntity<*>).toEqual(true)
        expect(department1).toEqual(department1)
        expect(department1).toEqual(department2)
        expect(department1).notToEqual(department3)
        expect(department2).notToEqual(department3)
    }

    /** Copying one object to another */
    @Test
    fun copyObject() {
        val employees = MyDatabase.employees // Employee metamodel
        val target: Employee = employees.new()
        val source: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        employees.utils().properties.forEach{
            target[it] = source[it]
        }

        expect(target.id).toEqual(source.id)
        expect(target.name).toEqual(source.name)
        expect(target.higherEducation).toEqual(source.higherEducation)
        expect(target.contractDay).toEqual(source.contractDay)
        expect(target.department).toEqual(source.department)
        expect(target).toEqual(source)
    }


    /** Clone an object to a new one */
    @Test
    fun cloneObject() {
        val employees = MyDatabase.employees // Employee metamodel
        val source: Employee = employees.new { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        val target: Employee = MyDatabase.utils().clone(source)

        expect(target.id).toEqual(source.id)
        expect(target.name).toEqual(source.name)
        expect(target.higherEducation).toEqual(source.higherEducation)
        expect(target.contractDay).toEqual(source.contractDay)
        expect(target.department).toEqual(source.department)
        expect(target).toEqual(source)
    }
}

/** Helper method to create new department */
private fun createDepartment(id: Int, name: String): Department =
    MyDatabase.departments.new {
        this.id = id
        this.name = name
    }