package org.ujorm.kotlin

import java.lang.IllegalStateException
import kotlin.reflect.KClass

interface Operator

interface Criterion<D : Any, out OP : Operator, out V : Any> {
    val operator: OP
    fun eval(domain : D) : Boolean
    fun not() = BinaryCriterion(this, BinaryOperator.NOT, this)
}

interface Key<D : Any, V : Any> {
    val name : String
    val domainClass : KClass<D>
    val valueClass : KClass<out V>
    val setter : Any
    val getter : Any

    /** Get a value from the domain object */
    fun of(domain : D) : V

    /** Set a value to the domain object */
    fun set(domain: D, value: V) : Unit

    fun operate(operator : ValueOperator, value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    infix fun EQ(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.EQ, value)
    }

    infix fun GT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.GT, value)
    }

    infix fun LT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.LT, value)
    }
}

open class KeyImpl<D : Any, V : Any> : Key<D, V> {
    constructor(name: String, domainClass: KClass<D>, valueClass: KClass<V>, setter: Any, getter: Any) {
        this.name = name
        this.domainClass = domainClass
        this.valueClass = valueClass
        this.setter = setter
        this.getter = getter
    }

    /** Get a value from the domain object */
    override fun of(domain: D): V {
        TODO("Not yet implemented")
    }

    /** Set a value to the domain object */
    override fun set(domain: D, value: V) {
        TODO("Not yet implemented")
    }

    override val name: String
    override val domainClass: KClass<D>
    override val valueClass: KClass<V>
    override val setter: Any
    override val getter: Any
}

enum class ValueOperator : Operator {
    EQ,
    LT,
    GT,
    LTE,
    GTE,
    ALL,
    NONE
}

enum class BinaryOperator : Operator {
    AND,
    OR,
    NOT,
    AND_NOT,
    OR_NOT;
}

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any>> {
    val left : Criterion<D, Operator, out Any>
    val right : Criterion<D, Operator, out Any>
    override val operator: BinaryOperator

    constructor(
        left: Criterion<D, out Operator, out Any>,
        operator: BinaryOperator,
        right: Criterion<D, out Operator, out Any>
    ) {
        this.left = left
        this.operator = operator
        this.right = right
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            BinaryOperator.AND -> left.eval(domain) && right.eval(domain)
            BinaryOperator.OR -> left.eval(domain) || right.eval(domain)
            BinaryOperator.AND_NOT -> left.eval(domain) && !right.eval(domain)
            BinaryOperator.OR_NOT -> left.eval(domain) || !right.eval(domain)
            else -> {
                throw UnsupportedOperationException("Unsupported operator: $operator")
            }
        }
    }
}

open class ValueCriterion<D : Any, out V : Any> : Criterion<D, ValueOperator, V> {
    val key : Key<D, out V>
    val value : V
    override val operator: ValueOperator

    constructor(key: Key<D, out V>, operator: ValueOperator, value: V) {
        this.key = key
        this.operator = operator
        this.value = value
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            ValueOperator.ALL -> true
            ValueOperator.NONE -> false
            ValueOperator.EQ -> key.of(domain) == value
            ValueOperator.GT ->  compare(key.of(domain), value) > 0
            ValueOperator.GTE -> compare(key.of(domain), value) >= 0
            ValueOperator.LT -> compare(key.of(domain), value) < 0
            ValueOperator.LTE -> compare(key.of(domain), value) <= 0
            else -> throw java.lang.UnsupportedOperationException("Unsupported operator $operator")
        }
    }

    /** Private comparator */
    private fun <T : Any> compare(a: T?, b: T?): Int {
        if (a === b) return 0
        if (a == null) return -1
        if (b == null) return 1

        return if (a is Comparable<*>) {
            //@Suppress("UNCHECKED_CAST")
            (a as Comparable<T>).compareTo(b)
        } else {
            throw IllegalStateException("Unsupported comparation for ${this.key.valueClass}" )
        }
    }

    infix fun AND(crn: Criterion<D, out Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND, crn)
    }
    infix fun OR (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR, crn)
    }
    infix fun AND_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND_NOT, crn)
    }
    infix fun OR_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR_NOT, crn)
    }
}

/** Interface of meta-model */
interface AbstractMetaModel {

}
