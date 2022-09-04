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


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.core.entity.*
import java.time.LocalDate

internal class UjormCoreTest {

    /** Test writing and reading to the object using the meta object. */
    @Test
    fun testReadWrite() {
        val employee = Employee(
            id = 11,
            name = "John",
            contractDay = LocalDate.now(),
            department = Department(2, "D")
        )
        val employees = ModelProvider.employees // Employee Entity meta-model
        val departments = ModelProvider.departments // Department Entity meta-model

        // Read and Write values by entity meta-model:
        val id : Int = employees.id[employee]
        val name : String = employees.name[employee]
        val contractDay : LocalDate = employees.contractDay[employee]
        val department : Department = employees.department[employee]
        val supervisor : Employee? = employees.supervisor[employee]
        employees.id[employee] = id
        employees.name[employee] = name
        employees.contractDay[employee] = contractDay
        employees.department[employee] = department
        employees.supervisor[employee] = supervisor

        // Composed properties:
        val employeeDepartmentId = (employees.department + departments.id)[employee]
        val employeeDepartmentName = (employees.department + departments.name)[employee]
        assertEq(employeeDepartmentId, 2) { "Department id must be 2" }
        assertEq(employeeDepartmentName, "D") { "Department name must be 'D'" }
    }

    /** Test conditions */
    @Test
    fun testConditions() {
        val employee = Employee(
            id = 11,
            name = "John",
            contractDay = LocalDate.now(),
            department = Department(2, "D")
        )
        val employees = ModelProvider.employees // Employee Entity meta-model
        val departments = ModelProvider.departments // Department Entity meta-model


        // Criterion conditions:
        val crn1 = employees.name EQ "Lucy"
        val crn2 = employees.id GT 1
        val crn3 = (employees.department + departments.id) LT 99
        val crn4 = crn1 OR (crn2 AND crn3)
        val crn5 = crn1.not() OR (crn2 AND crn3)
        val noValid: Boolean = crn1(employee)
        val isValid: Boolean = crn4(employee)

        // Criterion logs:
        assertFalse(noValid, { "crn1(employee)" })
        assertTrue(isValid, { "crn4(employee)" })
        assertEq(crn1.toString(), """Employee: name EQ "Lucy"""")
        assertEq(crn2.toString(), """Employee: id GT 1""")
        assertEq(crn3.toString(), """Employee: department.id LT 99""")
        assertEq(crn4.toString(), """Employee: (name EQ "Lucy") OR (id GT 1) AND (department.id LT 99)""")
        assertEq(crn5.toString(), """Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (department.id LT 99)""")
    }

    /** Sample of usage */
    @Test
    fun testExtendedFunctions() {
        val employee = Employee(
            id = 11,
            name = "John",
            contractDay = LocalDate.now(),
            department = Department(2, "D")
        )
        val employees = ModelProvider.employees // Employee Entity meta-model
        val departments = ModelProvider.departments // Department Entity meta-model

        val id: Int = employees.id[employee]
        val name: String = employees.name[employee] // Get a name of the employee
        val supervisor: Employee? = employees.supervisor[employee]

        assertEq(name, "John", { "employee name" })
        assertEq(id, 11, { "employee id" })
        assertEq(supervisor, null, { "employee supervisor " })

        employees.name[employee] = "James" // Set a name to the user
        employees.supervisor[employee] = null
        assertEq(employees.id.data().name, "id") { "property id" }
        assertEq(employees.id.toString(), "id") { "property id" }
        assertEq(employees.id.info(), "Employee.id") { "property id" }
        assertEq(employees.id(), "Employee.id") { "property id" }

        val properties = ModelProvider.employees.utils().properties
        assertEq(properties.size, 5) { "Count of properties" }
        assertEq(properties[0].data().name, "id") { "property id" }
        assertEq(properties[1].data().name, "name") { "property name" }
        assertEq(properties[2].data().name, "contract_day") { "property contract_day" } // User defined name
        assertEq(properties[3].data().name, "department") { "property department" }
        assertEq(properties[4].data().name, "supervisor") { "property supervisor" }

        // Value type
        assertEq(employees.id.data().valueClass, Int::class)
        assertEq(employees.contractDay.data().valueClass, LocalDate::class)

        // Entity type (alias domain type)
        assertEq(employees.id.data().entityClass, Employee::class)
        assertEq(employees.contractDay.data().entityClass, Employee::class)

        // Composed properties:
        val employeeDepartmentNameProp:
                Property<Employee, String> = employees.department + departments.name
        assertEq(employeeDepartmentNameProp.info(), "Employee.department.name")
        assertEq(employeeDepartmentNameProp.toString(), "department.name")
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

        assertEq(employee.id, 1)
        assertEq(employee.name, "John")
    }

    /** Helper methods */
    private fun <V> assertEq(currentValue: V, expectedValue: V, messageSupplier: (() -> String)? = null) {
        Assertions.assertEquals(expectedValue, currentValue, messageSupplier)
    }

    /** Helper methods */
    private fun assertTrue(condition: Boolean, message: (() -> String)? = null) {
        Assertions.assertTrue(condition, message)
    }

    /** Helper methods */
    private fun assertFalse(condition: Boolean, message: (() -> String)? = null) {
        Assertions.assertFalse(condition, message)
    }

}