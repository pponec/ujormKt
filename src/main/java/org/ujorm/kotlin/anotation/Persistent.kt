package org.ujorm.kotlin.anotation

import org.ujorm.kotlin.core.EntityInitializer
import kotlin.reflect.KClass

/**
 * Optional annotation to indicate a persistent entity.
 *
 * <h4>See more JPA anotations:</h4>
 *
 * https://thorben-janssen.com/key-jpa-hibernate-annotations/
 * https://docs.oracle.com/javaee/7/api/javax/persistence/Entity.html
 * https://docs.oracle.com/javaee/7/api/javax/persistence/package-frame.html
 **/
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Entity(
    /** Entity name */
    val name: String = "",
    /** Entity description */
    val description: String = "",
    /** An implementation of initializing a new entity. */
    val init: KClass<out EntityInitializer<*>> = EntityInitializer::class
)

/** Database table description */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
    val name: String = "",
    val schema: String = "",
)
