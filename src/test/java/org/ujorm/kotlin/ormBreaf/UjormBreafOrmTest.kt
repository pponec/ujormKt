package org.ujorm.kotlin.ormBreaf

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.orm.entity.MyDatabase
import org.ujorm.kotlin.orm.entity.*
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import java.time.LocalDate

internal class UjormBreafOrmTest {

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun simpleSelect_byInnerJoins() {
        val employees = MyDatabase.employees
        val departments = MyDatabase.departments

        // Hello employee!
        val employeeResult = MyDatabase
            .select(employees)
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(employeeResult.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Shorter notation:
        val theSameEmployee = MyDatabase
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(theSameEmployee.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Get ordered employee list:
        val employeeList = MyDatabase
            .where(employees.id GE 3)
            .orderBy(employees.id ASCENDING true)
            .limit(3)
            .offset(4)
            .toList()
        expect(employeeList).toHaveSize(3)
        expect(employeeList.first().department.name).toEqual("Office") // By a lazy loading

        // Ordered list with relation - by inner join:
        val employeesWithSelectedItems = MyDatabase
            .select(
                employees.id,
                employees.name,
                employees.department + departments.created,
            )
            .where((employees.name EQ "John") OR (employees.name EQ "Lucy"))
            .orderBy(employees.department + departments.created ASCENDING false)
            .toList()
        expect(employeesWithSelectedItems).toHaveSize(3)
        expect(employeesWithSelectedItems.first().department.created)
            .toEqual(LocalDate.of(2022, 12, 24))

        // Ordered list with relation - by outer join:
        val employeesByOuterJoin = MyDatabase
            .select(
                employees.id,
                employees.name,
                employees.superior + employees.name, // Optional superior's name
            )
            .where((employees.name EQ "John") OR (employees.name EQ "Lucy"))
            .orderBy(
                employees.superior + employees.name ASCENDING true,
                employees.name ASCENDING true)
            .toList()
        expect(employeesByOuterJoin).toHaveSize(3)
        expect(employeesByOuterJoin.first().superior?.name).toEqual("Black")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun extendedSelect() {
       TODO()
    }

}




