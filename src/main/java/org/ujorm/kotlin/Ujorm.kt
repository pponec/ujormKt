package org.ujorm.kotlin

import kotlin.reflect.KClass

interface Operator {}

interface Criterion<D : Any, out OP : Operator, out V : Any> {
    val operator: OP
    fun eval(domain : D) : Boolean
}

interface Key<D : Any, V : Any> {
    val name : String
    val domainClass : KClass<D>
    val valueClass : KClass<out V>

    /** Get value */
    fun of(domain : D) : V

    fun operate(operator : ValueOperator, value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, operator, value)
    }

    public infix fun EQ(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.EQ, value)
    }

    public infix fun GT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.GT, value)
    }

    public infix fun LT(value : V) : ValueCriterion<D, V> {
        return ValueCriterion(this, ValueOperator.LT, value)
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

public enum class ValueOperator : Operator {
    EQ,
    LT,
    GT,
    LTE,
    GTE,
}

public enum class BinaryOperator : Operator {
    AND,
    OR,
    NOT;
}

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any>> {
    val left : Criterion<D, Operator, Criterion<D, Operator, Any>>
    val right : Criterion<D, Operator, Criterion<D, Operator, Any>>
    override val operator: BinaryOperator
        get() = field

    constructor(
        left: Criterion<D, Operator, Criterion<D, Operator, *>>,
        operator: BinaryOperator,
        right: Criterion<D, Operator, Criterion<D, Operator, *>>
    ) {
        this.left = left
        this.operator = operator
        this.right = right
    }

    override fun eval(domain: D): Boolean {
        return when(operator) {
            BinaryOperator.AND -> left.eval(domain) && right.eval(domain)
            BinaryOperator.OR -> left.eval(domain) || right.eval(domain)
            BinaryOperator.NOT -> ! left.eval(domain)
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
        get() = field

    constructor(key: Key<D, out V>, operator: ValueOperator, value: V) {
        this.key = key
        this.operator = operator
        this.value = value
    }

    override fun eval(domain: D): Boolean {
        return true;
    }

    public infix fun AND(crn: Criterion<D, out Operator, out Any>): BinaryCriterion<D> {
//        val x : Criterion<D, Operator, Any> = this
//        val y : Criterion<D, Operator, Any> = crn
//        return BinaryCriterion(x, BinaryOperator.AND, y);
        TODO("Implement it")
    }
    public infix fun OR (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
//        return BinaryCriterion(this, BinaryOperator.OR, crn);
        TODO("Implement it")
    }
}
