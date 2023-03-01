package net.ponec.demo.ktorm.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate


/** Entity
 * We need to define entity classes firstly and bind table objects to them */
interface Employee2 : Entity<Employee2> {
    companion object : Entity.Factory<Employee2>()

    var id: Int
    var name: String
    var higherEducation: Boolean
    var contractDay: LocalDate
    var department: Department2
    var superior: Employee2?
}

/** Table */
class Employees2(alias: String? = null) : Table<Employee2>("employee", alias) {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val higherEducation = boolean("higherEducation").bindTo { it.higherEducation }
    val superiorId = int("superior_id").bindTo { it.superior?.id }  // pop: Why no reference() ?
    val departmentId = int("department_id").references(Departments2.instance) { it.department }
    val contractDay = date("contract_day").bindTo { it.contractDay }

    // Optional relations:
    val department = departmentId.referenceTable as Departments2

    // Helper methods
    companion object {
        val instance = Employees2()
    }
}

/**
 * Return a default entity sequence of Table
 */
val Database.employees get() = this.sequenceOf(Employees2.instance)