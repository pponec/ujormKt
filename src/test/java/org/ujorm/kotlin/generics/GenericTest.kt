package org.ujorm.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.ujorm.kotlin.generics.Generic
import kotlin.reflect.KClass

internal class GenericTest {

    @Test
    fun getGenericClassTest() {
        val generic = Generic()

        val list1: List<String> = arrayListOf()
        val clazz = generic.retrieveSomething(list1)
        val clazzExpected = String::class.java
        Assertions.assertEquals(clazzExpected, clazz)
    }

    @Test
    @Disabled("Compilation test only")
    fun genericTest() {
        val employees: Employees = null!!
        val employee = Employee(1, "Joe", null)

        // Get & set values (1):
        val id = employees.id[employee] // Long
        val name = employees.name[employee] // String
        val active = employees.active[employee] // Boolean?
        employees.id[employee] = id
        employees.name[employee] = name
        employees.active[employee] = active

        // Get & set values (2):
        val x1 = employees.id.get(employee) // Long
        val x2 = employees.name.get(employee) // String
        val x3 = employees.active.get(employee) // Boolean?
        employees.id.set(employee, x1)
        employees.name.set(employee, x2)
        employees.active.set(employee, x3)

        println("x1=$id, x2=$name, x3=$active")
    }

    interface PropertyNullable<D : Any, V : Any> {
        val entityClass: KClass<D>
        val valueClass: KClass<V>
        val getter: (D) -> V?
        val setter: (D, V?) -> Unit

        operator fun get(entity: D): V? = getter.invoke(entity)
        operator fun set(entity: D, value: V?): Unit = setter.invoke(entity, value)
    }

    interface Property<D : Any, V : Any> : PropertyNullable<D, V> {
        override val getter: (D) -> V
        override operator fun get(entity: D): V = getter.invoke(entity)
    }

    class Employee(
        var id: Long,
        var name: String,
        var active: Boolean?,
    )

    class Employees(
        val id: Property<Employee, Long> = null!!, // TODO: implement it!
        val name: Property<Employee, String> = null!!,
        val active: PropertyNullable<Employee, Boolean> = null!!,
    )

}