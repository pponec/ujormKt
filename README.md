# UjormKt

A very early prototype of the Kotlin library for modelling filters of domain objects.
Assembled filters work on common POJO objects.
The solution was inspired by the [Ujorm](https://pponec.github.io/ujorm/www/index.html) key-value framework, but this code is completely new.
Topical areas of use are:

- dynamic validations
- alerting
- modelling conditions for ORM

# What remains to be done

- building remote attribute models (via relationships) is not supported yet (including reading and writing values of POJO)
- API cleaning
- Serialize a `Criterion` object into `JSON` format and parsing the result back to the object.
- Integrate the model filters with the `JPA`/`Hibernate` framework
- the domain object model should be generated according to the original POJO objects in feature

## Usage:

Presentation of basic skills with entity model:

```kotlin
val employee = Employee(id = 11, name = "John", contractDay = LocalDate.now())
val employees = ModelProvider.employees

val id: Int = employees.id[employee]
val name: String = employees.name[employee] // Get a name of the employee
val supervisor: Employee? = employees.supervisor[employee]

assert(name == "John", { "employee name" })
assert(id == 11, { "employee id" })
assert(supervisor == null, { "employee supervisor " })

employees.name[employee] = "James" // Set a name to the user
employees.supervisor[employee] = null
assert(employees.id.name == "id") { "property name" }
assert(employees.id.toString() == "id") { "property name" }
assert(employees.id.info() == "Employee.id") { "property name" }
assert(employees.id() == "Employee.id") { "property name" }

val properties = ModelProvider.employees._properties
assert(properties.size == 5) { "Count of properties" }
assert(properties[0].name == "id") { "property name" }
assert(properties[1].name == "name") { "property name" }
assert(properties[2].name == "contract_day") { "property name" } // User defined name
assert(properties[3].name == "department") { "property name" }
assert(properties[4].name == "supervisor") { "property name" }
```

Building domain entity model:

```kotlin
data class User constructor(
    var id: Int,
    var nickname: String,
    var born: LocalDate,
    var department: Department = Department(1, "A"),
    var invitedFrom: User? = null
)

open class _User : EntityModel<User>(User::class) {
    val id = property { it.id }
    val nickname = property { it.nickname }
    val born = property { it.born }
    val department = property { it.department }
    val invitedFrom = propertyNullable("invited_from") { it.invitedFrom }
}

/** Initialize, register and close the entity model. */
val ModelProvider.user by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { _User().close() as _User }
```

## Class diagram

Common classes of the framework (released: 2021-07-06)

![Class diagram](docs/Ujorm.png)

An example implementation of this demo project (released: 2021-07-07)

![Class diagram](docs/Demo.png)

For more information see the 
[source code](https://github.com/pponec/ujormKt/blob/main/src/main/java/org/ujorm/kotlin/Demo.kt).