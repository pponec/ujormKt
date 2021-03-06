# ujormKt

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

```kotlin
val _user = ModelProvider.user

val crn1 = _user.nickname EQ "Pavel"
val crn2 = _user.id GT 1
val crn3 = _user.id LT 99
val crn4 = crn1 OR (crn2 AND crn3)
val crn5 = crn1.not() OR (crn2 AND crn3)
assert(crn1.toString() == """User: nickname EQ "Pavel"""")
assert(crn2.toString() == """User: id GT 1""")
assert(crn4.toString() == """User: (nickname EQ "Pavel") OR ((id GT 1) AND (id LT 99))""")
assert(crn5.toString() == """User: (NOT (nickname EQ "Pavel")) OR ((id GT 1) AND (id LT 99))""")

val user = User(id = 11, nickname = "Xaver", born = LocalDate.now())
val noValid: Boolean = crn1(user)
val isValid: Boolean = crn4(user)
assert(!noValid, { "crn1(user)" })
assert(isValid, { "crn4(user)" })

val userName: String = _user.nickname(user) // Get a name of the user
val userId: Int = _user.id(user)
val parent: User? = _user.parent(user)
assert(userName == "Xaver", { "userName" })
assert(userId == 11, { "userId" })
assert(parent == null, { "userId" })

_user.nickname(user, "James") // Set a name to the user
_user.parent(user, null)
assert(_user.id.name == "id", { "property name" })
assert(_user.id.toString() == "id", { "property name" })
assert(_user.id.info() == "User.id", { "property name" })
assert(_user.id() == "User.id", { "property name" })

val properties = ModelProvider.user._properties
assert(properties.size == 4, { "Count of properties" })
assert(properties[0].name == "id", { "property name" })
assert(properties[1].name == "nickname", { "property name" })
assert(properties[2].name == "born", { "property name" })

// Value type
assert(_user.id.valueClass == Int::class)
assert(_user.born.valueClass == LocalDate::class)

// Entity type (alias domain type)
assert(_user.id.entityClass == User::class)
assert(_user.born.entityClass == User::class)
``````

## Class diagram

Common classes of the framework (released: 2021-07-06)

![Class diagram](docs/Ujorm.png)

An example implementation of this demo project (released: 2021-07-07)

![Class diagram](docs/Demo.png)

For more information see the 
[source code](https://github.com/pponec/ujormKt/blob/main/src/main/java/org/ujorm/kotlin/Demo.kt).