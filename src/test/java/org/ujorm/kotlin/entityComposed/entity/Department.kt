package org.ujorm.kotlin.entityComposed.entity

import org.ujorm.kotlin.core.EntityModel
import org.ujorm.kotlin.core.PropertyNullable
import org.ujorm.kotlin.model.DomainEntityModel
import java.time.LocalDate
import java.time.LocalDateTime


/** An Department entity */
data class Department constructor(
    var id: Int,
    var name: String,
    var created: LocalDate = LocalDate.now().minusDays(1),
)

/** Model of the entity can be a generated class in the feature */
open class _Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}


/** Model of the entity can be a generated class in the feature */
open class Departments<D : Any>() : DomainEntityModel<Department>(_Departments()) {

    /** Direct property model */
    val core = _Departments().close() as _Departments

    /** Build the new Key */
    protected fun <V : Any> buildKey(property : PropertyNullable<*, V>) : PropertyNullable<D, V> {
        return if (super.baseInstance) {
            property as PropertyNullable<D, V>
        } else {
            TODO("Create composed property")
        }
    }

    val id : PropertyNullable<D, Int> get() = buildKey(core.id)
    val name : PropertyNullable<D, String> get() = buildKey(core.name)
    val created : PropertyNullable<D, LocalDate> = buildKey(core.created)
}

//
///** Model of the entity can be a generated class in the feature */
//open class Departments<D : Any> : PropertyNullable<D, Department> {
//    protected var entityModel__01 _Departments
//    protected var property: PropertyNullable<D, Department>
//
//    private constructor(entityModel: _Departments, property: PropertyNullable<D, Department>) {
//        this.entityModel = entityModel
//        this.property = property
//    }
//
//    companion object {
//         fun <D: Any, M: Any, V: Any> build(
//            entityModel: _Departments,
//            headProperty: PropertyNullable<D, Department>? = null
//        ) : Departments<D> {
//             val property : PropertyNullable<D, Department> =
//                 if (headProperty != null) {
//                     headProperty.plus(entityModel)
//                 } else {
//                     entityModel
//                 }
//            return Departments(entityModel, property)
//
//        }
//    }
//
//    /** Build the new Key */
//    protected fun <V : Any> buildKey(
//        property : PropertyNullable<*, V>) : PropertyNullable<D, V> {
//        return if (headProperty == null) {
//            property as PropertyNullable<D, V>
//        } else {
//            TODO("Create composed property")
//        }
//    }
//
//    override fun data(): PropertyMetadata<D, Department> {
//        TODO("Not yet implemented")
//    }
//
//    override fun get(entity: D): Department? {
//        TODO("Not yet implemented")
//    }
//
//    override fun set(entity: D, value: Department?) {
//        TODO("Not yet implemented")
//    }
//
//    // --- Property descriptors ---
//
//    val id : PropertyNullable<D, Int> get() = buildKey(entityModel.id)
//    val name get() = buildKey(entityModel.name)
//    val created get() = buildKey(entityModel.created)
//}

/** Initialize, register and close the entity model. */
val ModelProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    _Departments().close() as _Departments
}