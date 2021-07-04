# ujormKt

A very early prototype of a **key-value** implementation in Kotlin language.
The solution is based on a design from the [Ujorm](https://pponec.github.io/ujorm/www/index.html) framework.

## Usage:

```kotlin
val _user = ModelProvier.user
val crn1 = _user.name EQ "Pavel"
val crn2 = _user.id GT 1
val crn3 = _user.id LT 99
val crn4 = crn1 OR (crn2 AND crn3)
assert(crn1.toString() == "User(name EQ \"Pavel\")")
assert(crn2.toString() == "User(id GT 1)")

val user = User(11, "Xaver", LocalDate.now(), null)
val isValid : Boolean = crn4.eval(user)
val userName : String = _user.name.of(user)
val userId : Int = _user.id.of(user)
assert(isValid, { "crn4.eval(user)" })
assert(userName == "Xaver", { "Wrong userName" })
assert(userId == 11, { "Wrong userId" })

val nameId1 : String = _user.id.toString()
val nameId2 : String = _user.id()
assert(nameId1 == "id", { "Wrong nameId1" } )
assert(nameId2 == "id", { "Wrong nameId2" } )
``````



For more information see the 
[source code](https://github.com/pponec/ujormKt/blob/main/src/main/java/org/ujorm/kotlin/Demo.kt).