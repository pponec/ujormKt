package org.ujorm.kotlin.orm


internal object OrmUtils {

    /** Convert a camelcase text to the {@sample snake_case} */
    fun toSnackCase(camelCaseText : String) : String {
        val text = camelCaseText.trim()
        val result = StringBuilder(text.length)
        val whiteSpaces = " \t\n\r_"
        var lastCharType = 0 // 0-lower, 1-upper, 2-white,
        text.forEach { c ->
            when {
                whiteSpaces.contains(c) -> lastCharType = 2
                c.isUpperCase() || lastCharType == 2 -> {
                    when (lastCharType) {
                        0, 2 -> result.append('_').append(c.lowercaseChar())
                        1 -> result.append(c.lowercaseChar())
                    }
                    lastCharType = 1
                }
                else -> {
                    result.append(c)
                    lastCharType = 0
                }
            }
        }
        return result.toString()
    }
}
