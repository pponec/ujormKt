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
    val setter : Any
    val getter : Any

    /** Get a value from the domain object */
    fun of(domain : D) : V

    /** Set a value to the domain object */
    fun set(domain: D, value: V) : Unit

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
        get() = field
    override val domainClass: KClass<D>
        get() = field
    override val valueClass: KClass<V>
        get() = field
    override val setter: Any
        get() = field
    override val getter: Any
        get() = field
}

public enum class ValueOperator : Operator {
    EQ,
    LT,
    GT,
    LTE,
    GTE,
    ALL,
    NONE
}

public enum class BinaryOperator : Operator {
    AND,
    OR,
    AND_NOT,
    OR_NOT;
}

open class BinaryCriterion<D : Any> : Criterion<D, BinaryOperator, Criterion<D, Operator, Any>> {
    val left : Criterion<D, Operator, out Any>
    val right : Criterion<D, Operator, out Any>
    override val operator: BinaryOperator
        get() = field

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
        get() = field

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
            ValueOperator.GT ->  {
                val v1 : V = value
//                val v2 : V = key.of(domain)
//                return if (v1 is Comparable<*>) compareValues(v1, v2) < 0 else false
                return false
            }
            ValueOperator.LT -> false
            else -> throw java.lang.UnsupportedOperationException("Unsupported operator $operator")
        }
    }

    public infix fun AND(crn: Criterion<D, out Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND, crn);
    }
    public infix fun OR (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR, crn);
    }
    public infix fun AND_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.AND_NOT, crn);
    }
    public infix fun OR_NOT (crn: Criterion<D, Operator, out Any>): BinaryCriterion<D> {
        return BinaryCriterion(this, BinaryOperator.OR_NOT, crn);
    }
}
