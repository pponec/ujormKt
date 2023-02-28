package org.ujorm.kotlin.core.performance.ktorm

import net.ponec.demo.ktorm.entity.Department2
import net.ponec.demo.ktorm.entity.Employee2
import net.ponec.demo.ktorm.entity.Employees2
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.core.entity.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

internal class CorePerformanceKtormTest {

    val count = 10 // 10_000_000;

    @Test
    fun createEntities() {
        val array = arrayOf(Employee2{}, Employee2{});
        val start = LocalDateTime.now()

        for (i in 0 .. count) {
            // Create some new entities:
            val development: Department2 = Department2 {
                id = 1
                name = "development"
                created = LocalDate.of(2021, 10, 15)
            }
            val lucy: Employee2 = Employee2 {
                id = 2
                name = "Lucy"
                higherEducation = true
                contractDay = LocalDate.of(2022, 1, 1)
                superior = null
                department = development
            }
            val joe: Employee2 = Employee2 {
                id = 3
                name = "Joe"
                higherEducation = false
                contractDay = LocalDate.of(2022, 2, 1)
                superior = lucy
                department = development
            }

            array[0] = lucy
            array[1] = joe
        }

        val end = LocalDateTime.now()
        val duration = Duration.between(start, end).toMillis();
        println("time $duration ms")
    }

    /** Test writing and reading to the object using the metamodel. */
    @Disabled
    @Test
    fun readAndWriteByProperty() {
        val employees = Employees2.instance
        val employee: Employee2 = Employee2 { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        val start = LocalDateTime.now()
        for (i in 0 .. count) {
//            // Read and Write values by property descriptors:
//            val id: Int = employee[employees.id] as Int
//            val name: String = employee[employees.name]
//            val higherEducation: Boolean = employee[employees.higherEducation]
//            val contractDay: LocalDate = employee[employees.contractDay]
//            val department: Department = employee[employees.department]
//            val superior: Employee? = employee[employees.superior]
//
//            employee[employees.id] = id
//            employee[employees.name] = name
//            employee[employees.higherEducation] = higherEducation
//            employee[employees.contractDay] = contractDay
//            employee[employees.department] = department
//            employee[employees.superior] = superior
        }

        val end = LocalDateTime.now()
        val duration = Duration.between(start, end).toMillis();
        println("time $duration ms")
    }

    /** Test writing and reading to the object using the metamodel. */
    @Test
    fun readAndWriteByApi() {
        val employee: Employee2 = Employee2 { // Create new employee object
            id = 11
            name = "John"
            higherEducation = false
            contractDay = LocalDate.now()
            department = createDepartment(2, "D")
        }

        val start = LocalDateTime.now()
        for (i in 0 .. count) {
            // Read and Write values by property descriptors:
            val id: Int = employee.id
            val name: String = employee.name
            val higherEducation: Boolean = employee.higherEducation
            val contractDay: LocalDate = employee.contractDay
            val department: Department2 = employee.department
            val superior: Employee2? = employee.superior

            employee.id = id
            employee.name = name
            employee.higherEducation = higherEducation
            employee.contractDay = contractDay
            employee.department = department
            employee.superior = superior
        }

        val end = LocalDateTime.now()
        val duration = Duration.between(start, end).toMillis();
        println("time $duration ms")
    }


    /** Test writing and reading to the object using the metamodel. */
    @Test
    fun readAndWriteToMap() {
        val employees = Employees2.instance // Employee metamodel
        val employee = mutableMapOf<Any, Any?>(
            employees.id to 11,
            employees.name to "John",
            employees.higherEducation to false,
            employees.contractDay to LocalDate.now(),
            employees.department to createDepartment(2, "D"),
        )

        val start = LocalDateTime.now()
        for (i in 0 .. count) {
            // Read and Write values by property descriptors:
            val id: Int = employee[employees.id] as Int
            val name: String = employee[employees.name] as String
            val higherEducation: Boolean = employee[employees.higherEducation] as Boolean
            val contractDay: LocalDate = employee[employees.contractDay] as LocalDate
            val department: Department2 = employee[employees.department] as Department2
            val superior: Employee2? = employee[employees.superiorId] as Employee2?

            employee[employees.id] = id
            employee[employees.name] = name
            employee[employees.higherEducation] = higherEducation
            employee[employees.contractDay] = contractDay
            employee[employees.departmentId] = department
            employee[employees.superiorId] = superior
        }

        val end = LocalDateTime.now()
        val duration = Duration.between(start, end).toMillis();
        println("time $duration ms")
    }

    private fun createDepartment(id: Int, name: String): Department2 =
        Department2 {
            this.id = id
            this.name = name
        }

}