package org.ujorm.kotlin.core

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.core.entity.MyDatabase
import org.ujorm.kotlin.core.entity.employees
import org.ujorm.kotlin.orm.PropertyMap

internal class PropertyMapTest {

    @Test
    internal fun propertyMap() {
        val employees = MyDatabase.employees // Employee metamodel
        val map = PropertyMap()

        map[employees.id] = 10
        val id = map.get(employees.id)
        expect(id).toEqual(10)

        map[employees.name] = "Test"
        val name = map.get(employees.name)
        expect(name).toEqual("Test")

        map[employees.higherEducation] = true
        val higherEducation = map.get(employees.higherEducation)
        expect(higherEducation).toEqual(true)

        map[employees.superior] = null
        val superior = map.get(employees.superior)
        expect(superior).toEqual(null)

        val optionalSupervisor = map.getOptional(employees.superior)
        expect(optionalSupervisor.isPresent).toEqual(false)
    }
}






