package org.ujorm.tools.web.ao

import java.io.Reader
import java.io.Writer
import java.lang.reflect.InvocationTargetException

/** Reflection methods for the Servlet Request & Response classes  */
object Reflections {
    fun getServletReader(httpServletRequest: Any): Reader {
        return getAttribute(httpServletRequest, "getReader") as Reader
    }

    fun getServletWriter(httpServletResponse: Any): Writer {
        return getAttribute(httpServletResponse, "getWriter") as Writer
    }

    fun getParameterMap(httpServletRequest: Any): Map<String, Array<String>> {
        return getAttribute(httpServletRequest, "getParameterMap") as Map<String, Array<String>>
    }

    fun setCharacterEncoding(
        httpServletRequest: Any?,
        charset: String
    ) {
        val methodName = "setCharacterEncoding"
        if (httpServletRequest != null) try {
            val requestClass: Class<*> = httpServletRequest.javaClass
            val setCharsetEncoding = requestClass.getMethod(methodName, String::class.java)

            setCharsetEncoding.invoke(httpServletRequest, charset)
        } catch (e: NoSuchMethodException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                httpServletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        } catch (e: InvocationTargetException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                httpServletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        } catch (e: IllegalAccessException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                httpServletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        }
    }

    private fun getAttribute(servletRequest: Any, methodName: String): Any {
        try {
            val getReaderMethod = servletRequest.javaClass.getMethod(methodName)
            return getReaderMethod.invoke(servletRequest)
        } catch (e: NoSuchMethodException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                servletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        } catch (e: InvocationTargetException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                servletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        } catch (e: IllegalAccessException) {
            val msg = String.format(
                "Method does not exists: %s.%s()",
                servletRequest.javaClass.simpleName, methodName
            )
            throw RuntimeException(msg, e)
        }
    }
}
