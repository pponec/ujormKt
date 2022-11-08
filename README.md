# UjormKt

A very early prototype of the Kotlin library for modelling filters of domain objects.
Assembled filters work on common POJO objects.
The solution was inspired by the [Ujorm](https://pponec.github.io/ujorm/www/index.html) key-value framework, but this code is completely new.
Topical areas of use are:

- dynamic validations
- alerting
- modelling conditions for ORM

The ultimate goal of this project is to provide a programming interface (in Kotlin programming language) 
to build a database query model according to the following pattern. 
In doing so, the description of sessions is modeled by chaining metamodel attributes together, 
as if chaining common object attributes when reading object values. 
An entity here is an interface whose attributes are annotated with standard JPA specification annotations. 
However, in the first production release, I plan to support only a selected subset of JPA annotations. 
The SQL query modeling module will not have its own ORM implementation. 
The execution of database queries will then be delegated to an existing JPA implementation. 
Also in play is to create some clone of the Ujorm framework, adapted for Kotlin.

See the target `SELECT` example:

```kotlin
internal fun comprehensiveDatabaseSelect() {
    val employees = Database.employees
    val employeRows = Database.select(
        employees.id,
        employees.name,
        employees.department.name, // Required relation by the inner join!
        employees.supervisor.name, // Optional relation by the left outer join!
        employees.department.created,
    ).where((employees.department.id LE 1)
                AND (employees.department.name STARTS "D"))
        .orderBy((employees.department.created).desc())
        .toList()

    expect(employeRows).toHaveSize(1)
    expect(employeRows.first().department.name)
        .toEqual("Development")
    }
```

and an `INSERT` example:

```kotlin
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
```

# What remains to be done

- building remote attribute models (via relationships) is not supported yet (including reading and writing values of POJO)
- API cleaning
- Serialize a `Criterion` object into `JSON` format and parsing the result back to the object.
- Integrate the model filters with the `JPA`/`Hibernate` framework
- the domain object model should be generated according to the original POJO objects in feature

## Usage:

Presentation of basic skills with entity model:

```kotlin
val employee = Employee(
    id = 11, 
    name = "John", 
    contractDay = LocalDate.now(),
    department = Department(2, "D")
)
val employees = ModelProvider.employees // Employee Entity metamodel
val departments = ModelProvider.departments // Department Entity metamodel

// Read and Write values by entity metamodel:
val id : Int = employees.id[employee]
val name : String = employees.name[employee]
val contractDay : LocalDate = employees.contractDay[employee]
val department : Department = employees.department[employee]
val supervisor : Employee? = employees.supervisor[employee]
employees.id[employee] = id
employees.name[employee] = name
employees.contractDay[employee] = contractDay
employees.department[employee] = department
employees.supervisor[employee] = supervisor

// Composed properties:
val employeeDepartmentId = (employees.department + departments.id)[employee]
val employeeDepartmentName = (employees.department + departments.name)[employee]
assertEq(employeeDepartmentId, 2) { "Department id must be 2" }
assertEq(employeeDepartmentName, "D") { "Department name must be 'D'" }

// Criterion conditions:
val crn1 = employees.name EQ "Lucy"
val crn2 = employees.id GT 1
val crn3 = (employees.department + departments.id) LT 99
val crn4 = crn1 OR (crn2 AND crn3)
val crn5 = crn1.not() OR (crn2 AND crn3)
val noValid: Boolean = crn1(employee)
val isValid: Boolean = crn4(employee)

// Criterion logs:
assertFalse(noValid, { "crn1(employee)" })
assertTrue(isValid, { "crn4(employee)" })
assertEq(crn1.toString(), """Employee: name EQ "Lucy"""")
assertEq(crn2.toString(), """Employee: id GT 1""")
assertEq(crn3.toString(), """Employee: department.id LT 99""")
assertEq(crn4.toString(), """Employee: (name EQ "Lucy") OR (id GT 1) AND (department.id LT 99)""")
assertEq(crn5.toString(), """Employee: (NOT (name EQ "Lucy")) OR (id GT 1) AND (department.id LT 99)""")
```

Building domain entity model:

```kotlin
data class Employee constructor(
    var id: Int,
    var name: String,
    var contractDay: LocalDate,
    var department: Department = Department(1, "A"),
    var supervisor: Employee? = null
)

/** Model of the entity can be a generated class in the feature */
open class Employess : EntityModel<Employee>(Employee::class) {
    val id = property { it.id }
    val name = property { it.name }
    val contractDay = property("contract_day") { it.contractDay }
    val department = property { it.department }
    val supervisor = propertyNullable { it.supervisor }
}

/** Initialize, register and close the entity model. */
val ModelProvider.employees by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { 
    Employess().close() as Employess 
}
```

## Class diagram

Common classes of the framework (released: 2021-07-06)

![Class diagram](docs/Ujorm.png)

An example implementation of this demo project (released: 2021-07-07)

![Class diagram](docs/Demo.png)

For more information see the 
[source code](https://github.com/pponec/ujormKt/blob/main/src/main/java/org/ujorm/kotlin/Demo.kt).