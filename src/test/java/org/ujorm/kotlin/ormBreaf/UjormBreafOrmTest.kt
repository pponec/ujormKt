package org.ujorm.kotlin.ormBreaf

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.orm.entity.Database
import org.ujorm.kotlin.orm.entity.*
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import java.time.LocalDate

internal class UjormBreafOrmTest {

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun simpleSelect_byInnerJoins() {
        val employees = Database.employees
        val departments = Database.departments

        // Hello employee!
        val employeeResult = Database
            .select(employees)
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(employeeResult.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Slightly shorter notation:
        val theSameEmployee = Database
            .where(employees.id EQ 1)
            .toSingleObject()
        expect(theSameEmployee.id).toEqual(1)
        expect(employeeResult.name).toEqual("Joe")

        // Get ordered employee list:
        val employeeList = Database
            .where(employees.id GE 3)
            .orderBy(employees.id.asc())
            .limit(3)
            .offset(4)
            .toList()
        expect(employeeList).toHaveSize(3)
        expect(employeeList.first().department.name).toEqual("Office") // By a lazy loading

        // Ordered list with relation - by inner join:
        val employeesWithSelectedItems = Database
            .select(
                employees.id,
                employees.name,
                employees.department + departments.created,
            )
            .where((employees.name EQ "John") OR (employees.name EQ "Lucy"))
            .orderBy((employees.department + departments.created).desc())
            .toList()
        expect(employeesWithSelectedItems).toHaveSize(3)
        expect(employeesWithSelectedItems.first().department.created)
            .toEqual(LocalDate.of(2022, 12, 24))

        // Ordered list with relation - by outer join:
        val employeesByOuterJoin = Database
            .select(
                employees.id,
                employees.name,
                employees.supervisor + employees.name, // Optional supervisor's name
            )
            .where((employees.name EQ "John") OR (employees.name EQ "Lucy"))
            .orderBy((employees.supervisor + employees.name).asc(), employees.name.asc())
            .toList()
        expect(employeesByOuterJoin).toHaveSize(3)
        expect(employeesByOuterJoin.first().supervisor?.name).toEqual("Black")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun extendedSelect() {
       TODO()
    }

}




