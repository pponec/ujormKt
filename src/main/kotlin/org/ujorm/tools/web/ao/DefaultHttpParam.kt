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

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 * {
 * String value = Param.TEXT(ServletRequest, "my default value"); } enum Param implements HttpParam { REGEXP, TEXT;
 *
 * @Override public String toString() { return name().toLowerCase(); } }
</pre> *
 *
 * @author Pavel Ponec
 */
class DefaultHttpParam internal constructor(private val name: String, private val defaultValue: String) :
    HttpParameter {
    override fun defaultValue(): String {
        return defaultValue
    }

    override fun toString(): String {
        return name
    }

    override fun subSequence(start: Int, end: Int): CharSequence {
        return name.subSequence(start, end)
    }

    override fun length(): Int {
        return name.length
    }

    override fun charAt(index: Int): Char {
        return name[index]
    }
}
