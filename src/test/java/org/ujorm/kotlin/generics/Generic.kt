package org.ujorm.kotlin.generics

class Generic {

    /** Only type parameters of inline functions can be reified */
    inline fun <reified T: Any> retrieveSomething(list : List<T>): Any {
        return T::class.java
    }
}