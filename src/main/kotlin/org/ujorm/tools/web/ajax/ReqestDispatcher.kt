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
package org.ujorm.tools.web.ajax

import org.ujorm.tools.Assert
import org.ujorm.tools.web.HtmlElement
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.ao.IOConsumer
import org.ujorm.tools.web.ao.IOElement
import org.ujorm.tools.web.ao.IORunnable
import org.ujorm.tools.web.json.JsonBuilder
import org.ujorm.tools.web.request.RContext
import org.ujorm.tools.xml.config.HtmlConfig
import java.io.IOException
import java.util.logging.Logger

/**
 * A Reqest Dispatcher
 * @author Pavel Ponec
 */
class ReqestDispatcher(
    private val context: RContext,
    private val htmlConfig: HtmlConfig
) {
    private var done = false

    /**
     * Disable client cache
     */
    private val noCache = true

    constructor(context: RContext) : this("Info", context)

    constructor(
        title: CharSequence,
        context: RContext
    ) : this(
        context, HtmlConfig.ofDefault()
            .setTitle(title)
            .setNiceFormat()
    )

    val ajaxConfig: HtmlConfig
        get() = htmlConfig.cloneForAjax()

    /**
     * Registre new processor.
     *
     * @param key A key type of HttpParameter
     * @param processor processor
     * @return
     */
    @Throws(IOException::class)
    fun onParam(key: HttpParameter, processor: IOConsumer<JsonBuilder?>): ReqestDispatcher {
        Assert.notNull(key, "Parameter {} is required", "key")
        if (!done && key.of(context, false)) {
            JsonBuilder.Companion.of(context.writer(), ajaxConfig).use { builder ->
                done = true
                processor.accept(builder)
            }
        }
        return this
    }

    /**
     * The process writes to an element
     */
    @Throws(IOException::class)
    fun onDefaultToElement(defaultProcessor: IOElement) {
        if (!done) {
            HtmlElement.Companion.of(context.writer(), htmlConfig).use { html ->
                defaultProcessor.run(html)
            }
        }
    }

    /**
     * Process the request
     */
    @Throws(IOException::class)
    fun onDefault(defaultProcessor: IORunnable) {
        if (!done) {
            defaultProcessor.run()
        }
    }

    companion object {
        /**
         * Logger
         */
        private val LOGGER: Logger = Logger.getLogger(ReqestDispatcher::class.java.name)
    }
}
