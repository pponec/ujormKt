package demo

import java.lang.UnsupportedOperationException
import java.time.LocalDate
import kotlin.reflect.KClass

/** Sample of usage */
fun main(args: Array<String>) {
    val user = User(11, "Pavel", LocalDate.now(), null);

    val crn1 = _User.name EQ "Pavel"
    val crn2 = _User.id GT 1
    val crn3 = _User.id LT 99
    val crn4 = crn1 OR (crn2 AND crn3)

    val isValid : Boolean = crn4.eval(user)
    val userName : String = _User.name.of(user)
    //val parent : String = _User.name.parent.of(user) // TODO
    val id : Int = _User.id.of(user)

    println("Valid: $isValid, name=$userName, id=$id")
}

/** Domain object */
data class User constructor (
    val id: Int,
    private val name: String,
    private val born: LocalDate,
    private val parent: User?) {
}

/** Meta-model of the domain object */
object _User {
    val id : Key<User, Int> = KeyImpl("id", User::class, Int::class)
    val name : Key<User, String> = KeyImpl("name", User::class, String::class)
    val born : Key<User, LocalDate> = KeyImpl("name", User::class, LocalDate::class)
    val parent : Key<User, User> = KeyImpl("name", User::class, User::class)
}

interface Key<D : Any, V : Any> {
    val name : String
    val domainClass : KClass<D>
    val valueClass : KClass<out V>

    /** Get value */
    fun of(domain : D) : V

    fun operate(operator : ValueOperatoor, value : V) : Criterion<D, V> {
        return Criterion(this, operator, value)
    }

    public infix fun EQ(value : V) : Criterion<D, V> {
        return Criterion(this, ValueOperatoor.EQ, value)
    }

    public infix fun GT(value : V) : Criterion<D, V> {
        return Criterion(this, ValueOperatoor.GT, value)
    }

    public infix fun LT(value : V) : Criterion<D, V> {
        return Criterion(this, ValueOperatoor.LT, value)
    }
}

open class KeyImpl<D : Any, V : Any> : Key<D, V> {
    constructor(name: String, domainClass: KClass<D>, valueClass: KClass<V>) {
        this.name = name
        this.domainClass = domainClass
        this.valueClass = valueClass
    }

    override fun of(domain: D): V {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = field
    override val domainClass: KClass<D>
        get() = field
    override val valueClass: KClass<V>
        get() = field
}

public enum class ValueOperatoor {
    EQ,
    LT,
    GT,
    LTE,
    GTE,
}

public enum class BinaryOperatoor {
    AND,
    OR,
    NOT;
}

open class Criterion<D : Any, out V : Any> constructor (
    val key : Key<D, out V>,
    val operator : ValueOperatoor,
    val value : V,
) {

    /** Evalueate domain object */
    fun eval(domain : D) : Boolean {
        return true;
    }

    public infix fun <V : Any> AND(to: Criterion<D,V>): Criterion<D, Criterion<D, out Any>> {
        throw UnsupportedOperationException();
    }
    public infix fun <V : Any>  OR (to: Criterion<D,out V>): Criterion<D, Criterion<D, out Any>> {
        throw UnsupportedOperationException();
    }
}
