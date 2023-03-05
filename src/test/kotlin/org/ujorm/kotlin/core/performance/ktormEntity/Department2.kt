package org.ujorm.kotlin.core.performance.ktormEntity


import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate

/** Entity */
interface Department2 : Entity<Department2> {
    companion object : Entity.Factory<Department2>()

    var id: Int
    var name: String
    var created: LocalDate
    var members: String
}

/** Table */
class Departments2(alias: String? = null) : Table<Department2>("department", alias) {
    var id = int("id").primaryKey().bindTo { it.id }
    var name = varchar("name").bindTo { it.name }
    var created = date("created").bindTo { it.created }
    var members = varchar("members").bindTo { it.members }

    override fun aliased(alias: String) = Departments2(alias)

    // Helper methods
    companion object {
        val instance = Departments2()
    }
}

/**
 * Return a default entity sequence of Table
 */
val Database.departments get() = this.sequenceOf(Departments2.instance)