package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.core.Criterion
import org.ujorm.kotlin.core.CriterionProvider

/** Accept all employess type of senior */
internal class SeniorEmployeeFilter : CriterionProvider<Employee, Database> {

    override fun get(database: Database): Criterion<Employee, *, *> =
        database.employees.senior EQ true
}