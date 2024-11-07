package org.ujorm.tools.web.request

import java.io.Reader


class ManyMap {
    /** Internal map to store keys and their associated lists of values  */
    private val map: MutableMap<String, MutableList<String>> = HashMap()

    /** Method to add a value to the specified key  */
    fun put(key: String, vararg values: String) {
        for (value in values) {
            map.computeIfAbsent(key) { k: String? -> ArrayList(2) }.add(value)
        }
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list  */
    fun getList(key: String?): List<String> {
        return map.getOrDefault(key, emptyList())
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list  */
    fun get(key: String?): Array<String> {
        return getList(key).toTypedArray<String>()
    }

    /** Returns a key set  */
    fun keySet(): Set<String> {
        return map.keys
    }

    /** Create new Servlet request  */
    fun toRequest(reader: Reader): URequest {
        return URequestImpl(this, reader)
    }

    companion object {
        fun of(map: Map<String?, String?>): ManyMap {
            val result = ManyMap()
            map.forEach { (key: String?, value: String?) -> result.put(key!!, value!!) }
            return result
        }
    }
}