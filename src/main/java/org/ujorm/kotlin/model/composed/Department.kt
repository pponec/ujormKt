package org.ujorm.kotlin.model.composed
//
//import org.ujorm.kotlin.EntityModel
//import org.ujorm.kotlin.PropertyNullable
//import org.ujorm.kotlin.config.ModelProvider
//import java.time.LocalDate
//import kotlin.reflect.KClass
//
///** An Department entity */
//data class Department constructor(
//    var id: Int,
//    var name: String,
//    var created: LocalDate = LocalDate.now().minusDays(1),
//)
//
///** Model of the entity can be a generated class in the feature */
//open class Departments : EntityModel<Department>(Department::class) {
//    val id = property { it.id }
//    val name = property { it.name }
//    val created = property { it.created }
//}
//
///** Model of the entity can be a generated class in the feature */
//open class DepartmentS<D : Any, V: Any> : PropertyNullable<D, V>  {
//
//    val basicEntityModel : Departments get() = ModelProvider.departments
//
//    val id = property { it.id }
//    val extName = property { it.name }
//    val created = property { it.created }
//
//    //----
//
//    // PropertyNullable API:
//    override val index: UByte get() = TODO("Not yet implemented")
//    override val name: String get() = TODO("Not yet implemented")
//    override val entityClass: KClass<D> get() = TODO("Not yet implemented")
//    override val valueClass: KClass<out V> get() = TODO("Not yet implemented")
//    override val readOnly: Boolean get() = TODO("Not yet implemented")
//    override val nullable: Boolean get() = TODO("Not yet implemented")
//    override fun get(entity: D): V? { TODO("Not yet implemented") }
//    override fun set(entity: D, value: V?) { TODO("Not yet implemented") }
//}
//
//
//
///** Initialize, register and close the entity model. */
//val ModelProvider.departments by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
//    Departments().close() as Departments
//}