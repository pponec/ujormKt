package org.ujorm.kotlin.orm.entity

import org.ujorm.kotlin.core.Criterion
import org.ujorm.kotlin.core.CriterionProvider

/** Accept all employess type of higherEducation */
internal class SeniorEmployeeFilter : CriterionProvider<Employee, MyDatabase> {

    override fun get(database: MyDatabase): Criterion<Employee, *, *> =
        database.employees.higherEducation EQ true
}