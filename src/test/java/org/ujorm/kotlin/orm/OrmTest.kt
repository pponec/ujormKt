package org.ujorm.kotlin.orm

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.orm.entity.*
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import org.ujorm.kotlin.core.impl.TempModel
import java.time.LocalDate

internal class OrmTest {

    @Disabled("A comprehensive data selection")
    internal fun comprehensiveDatabaseSelect() {
        val employees = Database.employees
        val employeRows = Database.select(
            employees.id,
            employees.name,
            employees.department.name, // Required relation by the inner join
            employees.supervisor.name, // Optional relation by the left outer join
            employees.department.created,
        ).where((employees.department.id LE 1)
                    AND (employees.department.name STARTS "D"))
            .orderBy((employees.department.created).desc())
            .toList()

        expect(employeRows).toHaveSize(1)
        expect(employeRows.first().department.name)
            .toEqual("Development")
    }

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

        val employeRows = Database.select(
            employees.id,
            employees.name,
            employees.department.name, // Required relation by the inner join
            employees.supervisor.name, // Optional relation by the left outer join
            employees.department.created,
        ).where((employees.department.id LE 1)
                    AND (employees.department.name STARTS "A"))
            .orderBy((employees.department.created).desc())
            .toList()

        expect(employeRows).toHaveSize(3)
        expect(employeRows.first().department.created)
            .toEqual(LocalDate.of(2022, 12, 24))

        // Ordered list with relations:
        val employeesByOuterJoin = Database.select(
            employees.id,
            employees.name,
            employees.department.name, // Required relation by the inner join
            employees.supervisor.name, // Optional relation by the left outer join
        )
            .where(employees.department.name EQ "accounting")
            .orderBy(employees.supervisor.name.asc(), employees.name.desc())
            .toList()

        expect(employeesByOuterJoin).toHaveSize(3)
        expect(employeesByOuterJoin.first().supervisor?.name).toEqual("Black")
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun insertRows() {
        val development = Database.departments.new {
            name = "Development"
            created = LocalDate.of(2020, 10 , 1)
        }
        val lucy = Database.employees.new {
            name = "lucy"
            contractDay = LocalDate.of(2022, 1 , 1)
            supervisor = null
            department = development
        }
        val joe = Database.employees.new {
            name = "Joe"
            contractDay = LocalDate.of(2022, 2 , 1)
            supervisor = lucy
            department = development
        }

        Database.save(development, lucy, joe)
    }

    @Test
    @Disabled("Only a first draft of API is implemented")
    internal fun nativeQuery() {
        val employees = Database.employees //.entityAlias("e")
        val departments = Database.departments // .entityAlias("d")

        // Metamodel of the result:
        val db = object : TempModel() {
            val id = property(Int::class)
            val name = property(String::class)
            val created = property(LocalDate::class)
        }

        // Result object list:
        val result24 = Database.selectFor()
            .item(employees.id, "+", 10).to(db.id)
            .item(departments.name).to(db.created)
            .where(employees.department.id, "=", departments.id)
            .toList()
        result24.forEach{
            val id : Int = db.id[it]
            val name : String = db.name[it]
            val created : LocalDate = db.created[it]
            println("Db record: id = $id, name = $name, created = $created")
        }

    }
}






