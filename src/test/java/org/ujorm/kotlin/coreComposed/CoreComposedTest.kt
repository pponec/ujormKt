/*
 * Copyright 2022-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.kotlin.coreComposed

import org.junit.jupiter.api.Test
import org.ujorm.kotlin.coreComposed.entity.*
import java.time.LocalDate
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.AbstractEntity
import org.ujorm.kotlin.core.PropertyNullable

internal class CoreComposedTest {

    //@org.junit.jupiter.api.Disabled("Implementation is not finished")
    @Test
    fun testReadWrite() {
        val entities = Entities.close<Entities>() // Lock the metamodel first
        val employees = entities.employees // Employee Entity metamodel
        val employee: Employee = employees.new {
            id = 11
            name = "John"
            contractDay = LocalDate.parse("2022-11-19")
            department = createDepartment(2, "Development")
        }

        expect(employee is AbstractEntity<*>).toEqual(true)
        expect(employee.id).toEqual(11)
        expect(employee.name).toEqual("John")
        expect(employee.toString()).toEqual("Employee{id=11, name=\"John\"" +
                ", contractDay=2022-11-19" +
                ", department=Department{id=2, name=\"Development\",...}" +
                ", supervisor=null}")

        val emplId: PropertyNullable<Employee, Int> = employees.id
        expect(employee[emplId]).toEqual(11)
        val emplDepId: PropertyNullable<Employee, Int> = employees.department.id // (Fix it!)

        // Read and Write values by property descriptors:
        val id: Int? = employees.id[employee]
        val name: String? = employees.name[employee]
        val contractDay: LocalDate? = employees.contractDay[employee]
        val department: Department? = employees.department.get(employee)
        val supervisor: Employee? = employees.supervisor[employee]
        employees.id[employee] = id
        employees.name[employee] = name
        employees.contractDay[employee] = contractDay
        employees.department[employee] = department
        employees.supervisor[employee] = supervisor

        // Composed properties:
        val employeeDepartmentId : Int? = employee[employees.department.id] // (!)
        val employeeDepartmentName : String? = employee[employees.department.name] // (!)
        val employeeDepartment : Department? = employee[employees.department] // (!)

        val employeeDepartmentId2 : Int? = employee.get(employees.department.id)
        val employeeDepartmentName2 : String? = employee.get(employees.department.name)

        val propertyFromEmployeeToDepartment : Departments<Employee> = employees.department;
        val propertyExt : PropertyNullable<Employee, Department> = propertyFromEmployeeToDepartment;
        val employeeDepartment2 : Department? = employee.get(propertyFromEmployeeToDepartment)
        val employeeDepartment3 : Department? = propertyFromEmployeeToDepartment.get(employee)




        expect(employeeDepartmentId).toEqual(2)  // Department id must be 2
        expect(employeeDepartmentName).toEqual("D") // Department name must be 'D'
    }

    /** Create new department */
    private fun createDepartment(id: Int, name: String): Department =
        Entities.departments.new {
        this.id = id
        this.name = name
    }

}