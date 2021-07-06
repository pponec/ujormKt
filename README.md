# ujormKt

A very early prototype of the Kotlin library for modelling filters using a domain object meta-model.
The solution is based on a design from the [Ujorm](https://pponec.github.io/ujorm/www/index.html) key-value framework.
Topical areas of use are:

- validations
- alerting
- modelling conditions for ORM

# What remains to be done

- reading and writing remote attributes (via relationships) is not supported yet
- the domain object model should be generated according to the original POJO objects in feature
- API cleaning
- Integration of model filters with the Hibernate framework


## Usage:

```kotlin
val _user = ModelProvider.user
val crn1 = _user.name EQ "Pavel"
val crn2 = _user.id GT 1
val crn3 = _user.id LT 99
val crn4 = crn1 OR (crn2 AND crn3)
val crn5 = crn1.not() OR (crn2 AND crn3)
assert(crn1.toString() == "User: name EQ \"Pavel\"")
assert(crn2.toString() == "User: id GT 1")
assert(crn4.toString() == "User: (name EQ \"Pavel\") OR ((id GT 1) AND (id LT 99))")
assert(crn5.toString() == "User: (NOT (name EQ \"Pavel\")) OR ((id GT 1) AND (id LT 99))")

val user = User(id = 11, name = "Xaver", born = LocalDate.now())
val noValid : Boolean = crn1.eval(user)
val isValid : Boolean = crn4.eval(user)
val userName : String = _user.name.of(user)
val userId : Int = _user.id.of(user)
val parent : User? = _user.parent.of(user)
assert(!noValid, { "crn1.eval(user)" })
assert( isValid, { "crn4.eval(user)" })
assert(userName == "Xaver", { "userName" })
assert(userId == 11, { "userId" })
assert(parent == null, { "userId" })

_user.name.set(user, "James")
_user.parent.set(user, null)

val nameId1 : String = _user.id.toString()
val nameId2 : String = _user.id()
assert(nameId1 == "id", { "nameId1" } )
assert(nameId2 == "id", { "nameId2" } )
``````

## Class diagram

Common classes of the framework (released: 2021-07-06)

![Class diagram](docs/Ujorm.png)

An example implementation of this demo project (released: 2021-07-07)

![Class diagram](docs/Demo.png)

For more information see the 
[source code](https://github.com/pponec/ujormKt/blob/main/src/main/java/org/ujorm/kotlin/Demo.kt).