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
package org.ujorm.kotlin.entityComposed

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.entityComposed.entity.*
import java.time.LocalDate
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.PropertyNullable

internal class EntityComposedTest {

    @Disabled("Implementation is not finished")
    @Test
    fun testReadWrite() {
        val employees = ModelProvider.employees // Employee Entity metamodel
        val employee = employees.new {
            id = 11
            name = "John"
            contractDay = LocalDate.now()
            department = getDepartment(2, "D")
        }
        expect(employee.id).toEqual(11)
        expect(employee.name).toEqual("John")

        val emplDepId: PropertyNullable<Employee, Int> = employees.department.id // (!)

        // Read and Write values by entity metamodel:
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
        val employeeDepartmentId = employees.department.id[employee] // (!)
        val employeeDepartmentName = employees.department.name[employee]
        expect(employeeDepartmentId).toEqual(2)  // Department id must be 2
        expect(employeeDepartmentName).toEqual("D") // Department name must be 'D'
    }

    private fun getDepartment(id: Int, name: String) =
        ModelProvider.departments.new {
            this.id = id
            this.name = name
        }

}