package org.ujorm.kotlin.proxy

interface KDuck {

    fun quack(): String = "QUACK"
    fun name(): String
    fun age(): Int?
    val alias get() = "Donald"
}