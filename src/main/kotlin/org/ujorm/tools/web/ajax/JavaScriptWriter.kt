/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.Check
import org.ujorm.tools.web.Element
import org.ujorm.tools.web.Html
import org.ujorm.tools.web.ao.HttpParameter
import org.ujorm.tools.web.ao.Injector
import java.time.Duration
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * A prototype of ES6 Vanilla Javascript Writer of the Ujorm framework.
 *
 * @author Pavel Ponec
 */
class JavaScriptWriter(
    idleDelay: Duration,
    ajaxRequestParam: HttpParameter,
    sortRequestParam: HttpParameter,
    vararg inputSelectors: CharSequence
) : Injector {
    /** Javascript ajax request parameter  */
    protected val ajaxRequestParam: HttpParameter

    /** Javascript ajax request parameter  */
    protected val sortRequestParam: HttpParameter

    /** Input selectors  */
    protected val inputCssSelectors: Array<CharSequence>

    /** An AJAX delay to the input request  */
    protected var idleDelay: Duration = DEFAULT_DELAY

    /** An AJAX timeout of the input response  */
    protected var ajaxTimeout: Duration = DEFAULT_TIMEOUT

    /** Form selector  */
    protected var formSelector: String = Html.Companion.FORM

    /** On load submit request  */
    protected var onLoadSubmit: Boolean = false

    /** New line characters  */
    protected var newLine: CharSequence = "\n"

    /** A subtitle selector  */
    protected var subtitleSelector: CharSequence? = "?"

    /** A subtitle selector  */
    protected var errorMessage: CharSequence = "AJAX fails due"

    /** JavaScript version  */
    protected var version: Int = 1

    /** Javascript ajax request parameter  */
    protected var ajaxRequestPath: String = "_ajax"
    /** Set a function order name  */
    /** Function order of name  */
    var fceOrder: Int = 1
        protected set
    /** Set a function order name  */
    /** Ajax support  */
    var isAjax: Boolean = true
        protected set

    constructor() : this("form input:not([type=\"button\"])")

    constructor(vararg inputSelectors: CharSequence) : this(
        DEFAULT_DELAY,
        DEFAULT_AJAX_REQUEST_PARAM,
        DEFAULT_SORT_REQUEST_PARAM,
        *inputSelectors
    )

    init {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay")
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam")
        this.sortRequestParam = Assert.notNull(sortRequestParam, "sortRequestParam")
        this.inputCssSelectors = Assert.hasLength(inputSelectors, "inputSelectors")
    }

    fun setFormSelector(formSelector: String?): JavaScriptWriter {
        this.formSelector = Assert.notNull(formSelector, "formSelector")
        return this
    }

    fun setOnLoadSubmit(onLoadSubmit: Boolean): JavaScriptWriter {
        this.onLoadSubmit = onLoadSubmit
        return this
    }

    fun setNewLine(newLine: CharSequence): JavaScriptWriter {
        this.newLine = Assert.notNull(newLine, "newLine")
        return this
    }

    /** Assign a subtitle CSS selector  */
    fun setSubtitleSelector(subtitleSelector: CharSequence?): JavaScriptWriter {
        this.subtitleSelector = subtitleSelector
        return this
    }

    /** Assign an AJAX error message  */
    fun setErrorMessage(errorMessage: CharSequence?): JavaScriptWriter {
        this.errorMessage = Assert.hasLength(errorMessage, "errorMessage")
        return this
    }

    /** An AJAX timeout to get a response   */
    fun setAjaxTimeout(ajaxTimeout: Duration): JavaScriptWriter {
        this.ajaxTimeout = Assert.notNull(ajaxTimeout, "ajaxTimeout")
        return this
    }

    /** An AJAX delay to the input request  */
    fun setIdleDelay(idleDelay: Duration): JavaScriptWriter {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay")
        return this
    }

    /** Assign an AJAX timeout  */
    fun setAjaxRequestPath(ajaxRequestPath: String): JavaScriptWriter {
        this.ajaxRequestPath = ajaxRequestPath
        setVersion(2)
        return this
    }

    /** Assign an AJAX timeout  */
    fun setVersion(version: Int): JavaScriptWriter {
        this.version = version
        return this
    }

    /** Set a function order  */
    fun setSortable(fceOrder: Int): JavaScriptWriter {
        this.fceOrder = fceOrder
        return this
    }

    /** Set a function order  */
    fun setAjax(ajax: Boolean): JavaScriptWriter {
        this.isAjax = ajax
        return this
    }

    /**
     * Generate a Javascript
     */
    override fun write(parent: Element) {
        val inpSelectors = if (Check.hasLength(*inputCssSelectors))
            Stream.of(*inputCssSelectors).collect(Collectors.joining(", "))
        else
            "#!@"
        parent.addElement(Html.Companion.SCRIPT).use { js ->
            js.addRawText(newLine, "/* Script of ujorm.org *//* jshint esversion:6 */")
            if (isAjax) {
                js.addRawText(newLine, "const f", fceOrder, "={")
                js.addRawTexts(
                    newLine, "",
                    "ajaxRun:false, submitReq:false, delayMs:" + idleDelay.toMillis() + ", timeout:null,",
                    "init(e){",
                    " document.querySelector('$formSelector').addEventListener('submit',this.process,false);",
                    " document.querySelectorAll('$inpSelectors').forEach(i=>{",
                    "  i.addEventListener('keyup',e=>this.timeEvent(e),false);",
                    " });},"
                )
                js.addRawTexts(
                    newLine, "",
                    "timeEvent(e){",
                    " if(this.timeout)clearTimeout(this.timeout);",
                    " this.timeout=setTimeout(()=>{",
                    "  this.timeout=null;",
                    "  if(this.ajaxRun)this.submitReq=true;",
                    "  else this.process(null);",
                    " },this.delayMs);},"
                )
                js.addRawTexts(
                    newLine, "",
                    "process(e){",
                    " let pars=new URLSearchParams(new FormData(document.querySelector('$formSelector')));",
                    " if(e!==null){e.preventDefault();pars.append(e.submitter.name,e.submitter.value);}",
                    " fetch('" + (if (version == 2)
                        ajaxRequestPath
                    else
                        ("?$ajaxRequestPath=true")) + "', {",
                    "   method:'POST',",
                    "   body:pars,",
                    "   headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},",
                    " })",
                    " .then(response=>response.json())",
                    " .then(data=>{",
                    "   for(const key of Object.keys(data))",
                    "    if(key=='')eval(data[key]);",
                    "    else document.querySelectorAll(key).forEach(i=>{i.innerHTML=data[key];});",
                    "   if(this.submitReq){this.submitReq=false;this.process(e);}",  // Next submit the form
                    "   else{this.ajaxRun=false;}",
                    " }).catch(err=>{",
                    "   this.ajaxRun=false;",
                    "    document.querySelector('$subtitleSelector').innerHTML='$errorMessage: ' + err;",
                    " });",
                    "}"
                )
                js.addRawTexts(newLine, "};")
                js.addRawText(newLine, "document.addEventListener('DOMContentLoaded',e=>f", fceOrder, ".init(e));")
                if (onLoadSubmit) {
                    js.addRawText(newLine, "f", fceOrder, ".process(null);")
                }
            }
        }
    }

    companion object {
        /** Default AJAX request parameter name  */
        val DEFAULT_AJAX_REQUEST_PARAM: HttpParameter = HttpParameter.Companion.of("_ajax")

        /** Default AJAX request parameter name  */
        val DEFAULT_SORT_REQUEST_PARAM: HttpParameter = HttpParameter.Companion.of("_sort")

        /** Default duration  */
        val DEFAULT_DELAY: Duration = Duration.ofMillis(250)

        /** Default timeou  */
        val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(30)
    }
}
