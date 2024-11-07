package org.ujorm.tools.web.request

import org.ujorm.tools.web.ao.Reflections
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/** Servlet request context  */
class RContext @JvmOverloads constructor(
    private val uRequest: URequest = URequestImpl.Companion.of(),
    private val writer: Appendable? = StringBuilder()
) {
    fun request(): URequest {
        return uRequest
    }

    fun writer(): Appendable {
        return writer!!
    }

    /** Returns the last parameter, or the null value.  */
    fun getParameter(key: String): String? {
        return getParameter(key, null)
    }

    val parameterNames: Set<String?>
        /** Returns the parameter names  */
        get() = uRequest.parameterNames

    /** Returns the last parameter  */
    fun getParameter(key: String, defaultValue: String?): String? {
        val uRequest = request()
        if (uRequest != null) {
            val params = uRequest.getParameters(key)
            return if (params.size > 0) params[params.size - 1] else defaultValue
        }
        return defaultValue
    }

    companion object {
        val CHARSET: Charset = StandardCharsets.UTF_8

        /** HTTP Servlet Factory  */
        fun ofServletResponse(httpServletResponse: Any): RContext {
            return ofServlet(null, httpServletResponse)
        }

        /** HTTP Servlet Factory  */
        fun ofServlet(httpServletRequest: Any?, httpServletResponse: Any): RContext {
            Reflections.setCharacterEncoding(httpServletResponse, CHARSET.name())
            val writer: Appendable? = Reflections.getServletWriter(httpServletResponse)
            val ureq: URequest =
                if (httpServletRequest != null) URequest.Companion.ofRequest(httpServletRequest) else URequestImpl.Companion.of()
            return RContext(ureq, writer)
        }

        /** UContext from a map  */
        /** UContext from a map  */
        @JvmOverloads
        fun of(map: ManyMap = ManyMap()): RContext {
            return RContext(URequestImpl.Companion.ofMap(map), StringBuilder())
        }
    }
}
