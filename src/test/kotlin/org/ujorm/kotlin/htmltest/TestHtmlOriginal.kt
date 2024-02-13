package org.ujorm.kotlin.htmltest

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class TestHtmlOriginal {

    @Test
    fun buildHtmlPage() {
        val htmlContent = html {
            head {
                title("Example Page")
            }
            body {
                paragraph("This is a simple HTML page.")
                hyperlink("https://www.example.com", "Visit Example")
                text("Hello", "world!")
            }
        }

        val result = htmlContent.toString().trim()
        var expected = """
            <head>
            <title>Example Page</title>
            </head>
            <body>
            <p>This is a simple HTML page.</p>
            <a href="https://www.example.com">Visit Example</a>
            Hello world!
            </body>
        """.trimIndent()

        expect(result).toEqual(expected)
    }
}

class HTML {
    private val builder = StringBuilder()

    fun head(block: Head.() -> Unit) {
        val head = Head()
        head.block()
        builder.append(head)
    }

    fun body(block: Body.() -> Unit) {
        val body = Body()
        body.block()
        builder.append(body)
    }

    override fun toString(): String {
        return builder.toString()
    }
}

class Head {
    private val builder = StringBuilder()

    fun title(title: String) {
        builder.append("<title>$title</title>\n")
    }

    override fun toString(): String {
        return "<head>\n$builder</head>\n"
    }
}

class Body {
    private val builder = StringBuilder()

    fun paragraph(text: String) {
        builder.append("<p>$text</p>\n")
    }

    fun hyperlink(url: String, text: String) {
        builder.append("<a href=\"$url\">$text</a>\n")
    }

    fun text(vararg texts: String) {
        for ((i, text) in texts.withIndex()) {
            if (i > 0) {
                builder.append(' ')
            }
            builder.append(text)
        }
        builder.append('\n')
    }

    override fun toString(): String {
        return "<body>\n$builder</body>\n"
    }
}

fun html(htmlFun: HTML.() -> Unit): HTML {
    val html = HTML()
    html.htmlFun()
    return html
}
