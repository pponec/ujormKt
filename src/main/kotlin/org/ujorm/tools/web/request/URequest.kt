package org.ujorm.tools.web.request

import org.ujorm.tools.web.ao.Reflections
import java.io.*

interface URequest {
    /** Request Reader  */
    val reader: Reader

    /** Parameter provider  */
    fun getParameters(key: String?): Array<String?>

    /** Parameter provider  */
    val parameterNames: Set<String?>

    companion object {
        /** Convert the HttpServletRequest to the URequest  */
        fun ofRequest(httpServletRequest: Any?): URequest {
            Reflections.setCharacterEncoding(httpServletRequest, RContext.Companion.CHARSET.name())
            return object : URequest {
                var paramMap: Map<String?, Array<String?>?>? = null

                override val reader: Reader
                    get() = Reflections.getServletReader(httpServletRequest!!)

                override fun getParameters(key: String?): Array<String?> {
                    if (httpServletRequest != null) {
                        val paramMap = getMap(httpServletRequest)
                        val result = paramMap[key]
                        return result ?: URequestImpl.Companion.emptyTexts
                    } else {
                        return URequestImpl.Companion.emptyTexts
                    }
                }

                override val parameterNames: Set<String?>
                    get() = getMap(httpServletRequest!!).keys

                fun getMap(httpServletRequest: Any): Map<String?, Array<String?>?> {
                    if (paramMap == null) {
                        paramMap = Reflections.getParameterMap(httpServletRequest)
                    }
                    return paramMap ?: emptyMap<String, Array<String>>()
                }
            }
        }
    }
}
