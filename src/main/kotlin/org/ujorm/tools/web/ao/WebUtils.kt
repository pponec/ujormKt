/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao

import java.util.stream.Stream

/**
 * Static method for common use
 *
 * @author Pavel Ponec
 */
object WebUtils {
    /** Check if any attribute is typeof the Renderer  */
    fun <V : Any?> isType(type: Class<*>, vararg items: V): Boolean {
        var result = false
        for (item in items) {
            if (type.isInstance(item)) {
                result = true
                break
            }
        }
        return result
    }

    /** Check if any attribute is typeof the Renderer  */
    fun isType(type: Class<*>, items: Stream<Any?>): Boolean {
        val result = booleanArrayOf(false)
        items.filter { t: Any? -> !result[0] }
            .forEach { t: Any? ->
                if (type.isInstance(t)) {
                    result[0] = true
                }
            }
        return result[0]
    }
}
