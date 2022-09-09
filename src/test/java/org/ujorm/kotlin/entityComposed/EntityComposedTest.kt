package org.ujorm.kotlin.entityComposed

import org.junit.jupiter.api.Test
import org.ujorm.kotlin.AbstractTest
import org.ujorm.kotlin.entityComposed.entity.*
import java.time.LocalDate


internal class EntityComposedTest: AbstractTest() {

    @Test
    @Deprecated("TODO")
    fun testReadWrite() {
        val employee = Employee(
            id = 11,
            name = "John",
            contractDay = LocalDate.now(),
            department = Department(2, "D")
        )
        val employees = ModelProvider.employees // Employee Entity meta-model
        val departments = ModelProvider.departments // Department Entity meta-model

//        // Read and Write values by entity meta-model:
//        val id : Int = employees.id[employee]
//        val name : String = employees.name[employee]
//        val contractDay : LocalDate = employees.contractDay[employee]
//        val department : Department = employees.department[employee]
//        val supervisor : Employee? = employees.supervisor[employee]
//        employees.id[employee] = id
//        employees.name[employee] = name
//        employees.contractDay[employee] = contractDay
//        employees.department[employee] = department
//        employees.supervisor[employee] = supervisor

        // Composed properties:
        val employeeDepartmentId = employees.department.id[employee] // !!!
        val employeeDepartmentName = employees.department.name[employee]
        assertEq(employeeDepartmentId, 2) { "Department id must be 2" }
        assertEq(employeeDepartmentName, "D") { "Department name must be 'D'" }
    }

}