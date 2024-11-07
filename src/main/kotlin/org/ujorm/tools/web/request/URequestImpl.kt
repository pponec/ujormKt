package org.ujorm.tools.web.request

import java.io.CharArrayReader
import java.io.Reader

class URequestImpl(private val map: ManyMap, override val reader: Reader) : URequest {
    override fun getParameters(key: String?): Array<String?> {
        val result = map[key]
        return result ?: emptyTexts
    }

    override val parameterNames: Set<String?>
        get() = map.keySet()

    fun setParameter(name: String, value: String) {
        map.put(name, value)
    }

    companion object {
        val emptyTexts: Array<String?> = arrayOfNulls(0)

        fun ofMap(map: ManyMap): URequestImpl {
            return URequestImpl(map, CharArrayReader(CharArray(0)))
        }

        fun of(): URequestImpl {
            return URequestImpl(ManyMap(), CharArrayReader(CharArray(0)))
        }
    }
}
