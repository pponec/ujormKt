/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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
package org.ujorm.tools.web

/** A proxy for a HTML element
 * <nr>NOTE: All fields in interface are public static final, i.e. they are constants. </nr> */
interface Html {
    companion object {
        // --- Element names ---
        /** Body element  */
        const val HTML: String = "html"

        /** Head element  */
        const val HEAD: String = "head"

        /** Horizontal rule (line)  */
        const val HR: String = "hr"

        /** Meta element  */
        const val META: String = "meta"

        /** Body element  */
        const val BODY: String = "body"

        /** Title element  */
        const val TITLE: String = "title"

        /** Link element  */
        const val LINK: String = "link"

        /** Style element  */
        const val STYLE: String = "style"

        /** Script element  */
        const val SCRIPT: String = "script"

        /** Division in an HTML document.  */
        const val DIV: String = "div"

        /** Preformatted text.  */
        const val PRE: String = "pre"

        /** Span element  */
        const val SPAN: String = "span"

        /** Paragraph element  */
        const val P: String = "p"

        /** Form element  */
        const val FORM: String = "form"

        /** Heading prefix  */
        const val HEADING_PREFIX: String = "h"

        /** Heading element level 1  */
        const val H1: String = HEADING_PREFIX + 1

        /** Heading element level 2  */
        const val H2: String = HEADING_PREFIX + 2

        /** Heading element level 3  */
        const val H3: String = HEADING_PREFIX + 3

        /** Table element  */
        const val TABLE: String = "table"

        /** Table header group  */
        const val THEAD: String = "thead"

        /** Table body group  */
        const val TBODY: String = "tbody"

        /** Table header cell  */
        const val TH: String = "th"

        /** Table row element  */
        const val TR: String = "tr"

        /** Table detail element  */
        const val TD: String = "td"

        /** Image element  */
        const val IMAGE: String = "img"

        /** Label element  */
        const val LABEL: String = "label"

        /** Input element  */
        const val INPUT: String = "input"

        /** Input element  */
        const val TEXT_AREA: String = "textarea"

        /** Select element  */
        const val SELECT: String = "select"

        /** Option element  */
        const val OPTION: String = "option"

        /** Button  */
        const val BUTTON: String = "button"

        /** Anchor element  */
        const val A: String = "a"

        /** Unordered list elements (root)  */
        const val UL: String = "ul"

        /** Ordered list elements (root)  */
        const val OL: String = "ol"

        /** Some item element */
        const val LI: String = "li"

        /** A line break  */
        const val BR: String = "br"

        /** Field set  */
        const val FIELDSET: String = "fieldset"

        /** Legend  */
        const val LEGEND: String = "legend"

        // --- Attribute names ---
        const val A_ACTION: String = "action"

        /** Alternate text  */
        const val A_ALT: String = "alt"

        @Deprecated("Attribute {@code cellpadding} is not supported in HTML 5. ")
        const val A_CELLPADDING: String = "cellpadding"

        @Deprecated("Attribute {@code cellspacing} is not supported in HTML 5. ")
        const val A_CELLSPACING: String = "cellspacing"
        const val A_CHARSET: String = "charset"
        const val A_CHECKED: String = "checked"
        const val A_CLASS: String = "class"
        const val A_CONTENT: String = "content"
        const val A_FOR: String = "for"
        const val A_HREF: String = "href"
        const val A_HTTP_EQUIV: String = "http-equiv"
        const val A_ID: String = "id"

        /** Language attribute of the HTML pagee (e.g. "en")   */
        const val A_LANG: String = "lang"

        /** Reference of the programing language (e.g. "javascript")  */
        const val A_LANGUAGE: String = "language"
        const val A_MAXLENGTH: String = "maxlength"
        const val A_MEDIA: String = "media"
        const val A_METHOD: String = "method"
        const val A_NAME: String = "name"

        /** A short hint to describe the expected value of an input field  */
        const val A_PLACEHOLDER: String = "placeholder"
        const val A_READONLY: String = "readonly"
        const val A_REL: String = "rel"
        const val A_SELECTED: String = "selected"
        const val A_SRC: String = "src"

        /** CSS style  */
        const val A_STYLE: String = "style"
        const val A_TARGET: String = "target"
        const val A_TYPE: String = "type"

        /** A tooltip of an element */
        const val A_TITLE: String = "title"
        const val A_VALUE: String = "value"
        const val A_ROWS: String = "rows"
        const val A_COLS: String = "cols"
        const val A_ROWSPAN: String = "cols"
        const val A_COLSPAN: String = "colspan"
        const val A_ONCLICK: String = "onclick"

        // --- Attribute values ---
        const val V_BLANK: String = "_blank"
        const val V_CHECKBOX: String = "checkbox"

        /** Default FORM method by the W3C standard  */
        const val V_GET: String = "get"
        const val V_HIDDEN: String = "hidden"
        const val V_POST: String = "post"
        const val V_RESET: String = "reset"
        const val V_SUBMIT: String = "submit"
        const val V_STYLESHEET: String = "stylesheet"
        const val V_TEXT: String = "text"
        const val V_PASSWORD: String = "password"
        const val V_TEXT_CSS: String = "text/css"
    }
}
