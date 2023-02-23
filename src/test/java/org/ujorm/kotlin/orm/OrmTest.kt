package org.ujorm.kotlin.orm

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.orm.entity.*
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.impl.TempModel
import java.time.LocalDate

internal class OrmTest {

    @Test
    @Disabled("A comprehensive data selection")
    internal fun comprehensiveDatabaseSelect() {
        val employees: Employees = MyDatabase.employees // Employee metamodel
        val departments: Departments = MyDatabase.departments // Employee metamodel
        val employeRows: List<Employee> = MyDatabase.select(
            employees.id,
            employees.name,
            employees.department + departments.name, // Required relation by the inner join
            employees.superior + employees.name, // Optional relation by the left outer join
        ).where((employees.department + departments.id LE 1)
                    AND (employees.department + departments.name STARTS "D"))
            .orderBy(
                employees.department + departments.name ASCENDING false,
                employees.name ASCENDING true
            )
            .toList()

        expect(employeRows).toHaveSize(1)
        expect(employeRows.first().department.name)
            .toEqual("Development")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun simpleSelect_byInnerJoins() {
        val employees = MyDatabase.employees
        val departments = MyDatabase.departments

        // Hello, selected employee!
        val employeeResult = MyDatabase.select(employees)
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(employeeResult.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Shorter notation:
        val theSameEmployee = MyDatabase.where(employees.id EQ 1)
            .toSingleObject()
        expect(theSameEmployee.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Get ordered employee list:
        val employeeList = MyDatabase.where(employees.id GE 3)
            .orderBy(employees.id ASCENDING true)
            .limit(3)
            .offset(4)
            .toList()

        expect(employeeList).toHaveSize(3)
        expect(employeeList.first().department.name).toEqual("Office") // By a lazy loading

        val employeRows = MyDatabase.select(
            employees.id,
            employees.name,
            employees.department + departments.name, // Required relation by the inner join
            employees.superior + employees.name, // Optional relation by the left outer join
            employees.department + departments.created,
        ).where((employees.department + departments.id LE 1)
                    AND (employees.department + departments.name STARTS "A"))
            .orderBy(employees.department + departments.created ASCENDING false)
            .toList()

        expect(employeRows).toHaveSize(3)
        expect(employeRows.first().department.created)
            .toEqual(LocalDate.of(2022, 12, 24))

        // Ordered list with relations:
        val employeesByOuterJoin = MyDatabase.select(
            employees.id,
            employees.name,
            employees.department + departments.name, // Required relation by the inner join
            employees.superior + employees.name, // Optional relation by the left outer join
        )
            .where(employees.department + departments.name EQ "accounting")
            .orderBy(
                employees.superior + employees.name ASCENDING true,
                employees.name ASCENDING false)
            .toList()

        expect(employeesByOuterJoin).toHaveSize(3)
        expect(employeesByOuterJoin.first().superior?.name).toEqual("Black")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun insertRows() {
        val development: Department = MyDatabase.departments.new {
            name = "Development"
            created = LocalDate.of(2020, 10, 1)
        }
        val lucy: Employee = MyDatabase.employees.new {
            name = "lucy"
            contractDay = LocalDate.of(2022, 1, 1)
            superior = null
            department = development
        }
        val joe: Employee = MyDatabase.employees.new {
            name = "Joe"
            contractDay = LocalDate.of(2022, 2, 1)
            superior = lucy
            department = development
        }
        MyDatabase.save(development, lucy, joe)
    }

    @Test
    @Disabled("Select native query to a map)")
    internal fun nativeQueryToMaps() {
        val employees = MyDatabase.employees //.entityAlias("e")
        val departments = MyDatabase.departments // .entityAlias("d")

        // Metamodel of the result:
        val db = object : TempModel() {
            val id = property(Int::class)
            val name = property(String::class)
            val created = property(LocalDate::class)
        }

        // Result object list:
        val result = MyDatabase.selectToMaps()
            .item(employees.id, "+", 10).to(db.id)
            .item(departments.name).to(db.name)
            .where(employees.department + departments.id, "=", departments.id)
            .toMaps()

        result.forEach{ map ->
            val id : Int = map[db.id]
            val name : String = map[db.name]
            val created : LocalDate = map[db.created]
            println("Db record: id = $id, name = $name, created = $created")
        }
    }

}






