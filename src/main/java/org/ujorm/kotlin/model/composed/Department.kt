package org.ujorm.kotlin.model.composed

import org.ujorm.kotlin.EntityModel
import org.ujorm.kotlin.PropertyMetadata
import org.ujorm.kotlin.PropertyNullable
import java.time.LocalDate


/** An Department entity */
data class Department constructor(
    var id: Int,
    var name: String,
    var created: LocalDate = LocalDate.now().minusDays(1),
)

/** Model of the entity can be a generated class in the feature */
open class Departments : EntityModel<Department>(Department::class) {
    val id = property { it.id }
    val name = property { it.name }
    val created = property { it.created }
}

/** Model of the entity can be a generated class in the feature */
open class DepartmentS<D : Any> : PropertyNullable<D, Department>  {

    protected var primaryKey: PropertyNullable<D, Department>? = null
    protected var originalEntityModel : Departments

    /* Primary constructor **/
    constructor(originalEntityModel: Departments) :
            this(null, originalEntityModel)

    /** Extended constructor */
    constructor(primaryKey: PropertyNullable<D, Department>?,
                originalEntityModel: Departments,) {
        this.primaryKey = primaryKey
        this.originalEntityModel = originalEntityModel
    }

    /** Provides any Key */
    protected fun <V : Any> buildKey(property : PropertyNullable<*, V>) : PropertyNullable<D, V> {
        return if (primaryKey == null) {
            property as PropertyNullable<D, V>
        } else {
            TODO("Create composed property")
        }
    }

    override fun data(): PropertyMetadata<D, Department> {
        TODO("Not yet implemented")
    }

    override fun get(entity: D): Department? {
        TODO("Not yet implemented")
    }

    override fun set(entity: D, value: Department?) {
        TODO("Not yet implemented")
    }

    // --- Property descriptors ---

    val id : PropertyNullable<D, Int> get() = buildKey(originalEntityModel.id)
    val name get() = buildKey(originalEntityModel.name)
    val created get() = buildKey(originalEntityModel.created)
}

/** Initialize, register and close the entity model. */
val ModelProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Departments().close() as Departments
}