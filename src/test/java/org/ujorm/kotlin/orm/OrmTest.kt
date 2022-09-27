package org.ujorm.kotlin.orm

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.orm.entity.*
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import java.time.LocalDate

internal class OrmTest {

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun simpleSelect_byInnerJoins() {
        val employees = Database.employees

        // Hello, selected employee!
        val employeeResult = Database.select(employees)
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(employeeResult.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Shorter notation:
        val theSameEmployee = Database.where(employees.id EQ 1)
            .toSingleObject()
        expect(theSameEmployee.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Get ordered employee list:
        val employeeList = Database.where(employees.id GE 3)
            .orderBy(employees.id.asc())
            .limit(3)
            .offset(4)
            .toList()

        expect(employeeList).toHaveSize(3)
        expect(employeeList.first().department.name).toEqual("Office") // By a lazy loading

        // Ordered list with relation - by inner join:
        val employeesWithSelectedItems = Database.select(
                employees.id,
                employees.name,
                employees.department.created,
                employees.supervisor.name,
            )
            .where((employees.department.id LE 1) AND (employees.department.id LE 3))
            .orderBy((employees.department.created).desc())
            .toList()

        expect(employeesWithSelectedItems).toHaveSize(3)
        expect(employeesWithSelectedItems.first().department.created)
            .toEqual(LocalDate.of(2022, 12, 24))

        // Ordered list with relation - by outer join:
        val employeesByOuterJoin = Database.select(
                employees.id,
                employees.name,
                employees.supervisor.name, // Optional supervisor's name by outer join
            )
            .where(employees.department.name EQ "accounting")
            .orderBy(employees.supervisor.name.asc(), employees.name.desc())
            .toList()

        expect(employeesByOuterJoin).toHaveSize(3)
        expect(employeesByOuterJoin.first().supervisor?.name).toEqual("Black")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun insert() {
        val development = Database.departments.new().apply {
            name = "development"
            created = LocalDate.now()
        }
        val lucy = Database.employees.new().apply {
            name = "lucy"
            contractDay = LocalDate.of(2022, 1 , 1)
            supervisor = null
            department = development
        }
        val joe = Database.employees.new().apply {
            name = "Joe"
            contractDay = LocalDate.of(2022, 2 , 1)
            supervisor = lucy
            department = development
        }

        Database.save(development, lucy, joe)
    }
}




