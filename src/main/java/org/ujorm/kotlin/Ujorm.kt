package demo

import java.lang.UnsupportedOperationException
import java.time.LocalDate
import kotlin.reflect.KClass



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

    public infix fun <V : Any> AND(to: Criterion<D,out V>): Criterion<D, Criterion<D, out Any>> {
        throw UnsupportedOperationException();
    }
    public infix fun <V : Any>  OR (to: Criterion<D,out V>): Criterion<D, Criterion<D, out Any>> {
        throw UnsupportedOperationException();
    }
}
