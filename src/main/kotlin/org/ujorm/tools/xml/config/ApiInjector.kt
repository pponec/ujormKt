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
package org.ujorm.tools.xml.config

import org.ujorm.tools.xml.ApiElement

/**
 * An common element writer
 * @author Pavel Ponec
 */
fun interface ApiInjector {
    /**
     * Performs an operation on the argument element.
     *
     * @param element An element to write
     */
    fun write(element: ApiElement<*>)
}
