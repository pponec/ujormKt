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

        map[employees.senior] = true
        val senior = map.get(employees.senior)
        expect(senior).toEqual(true)

        map[employees.supervisor] = null
        val supervisor = map.get(employees.supervisor)
        expect(supervisor).toEqual(null)

        val optionalSupervisor = map.getOptional(employees.supervisor)
        expect(optionalSupervisor.isPresent).toEqual(false)
    }
}






