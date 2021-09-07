package org.ujorm.kotlin.proxy;

interface JDuck {
    default String quack() {
        return "QUACK";
    }

    String name();

    Integer age();
}