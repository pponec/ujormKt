package org.ujorm.kotlin.script

import ch.tutteli.atrium.api.fluent.en_GB.message
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test
import org.openjdk.nashorn.api.scripting.ClassFilter
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.io.File
import kotlin.reflect.KClass

internal class ScriptJava {

    companion object {
        private const val CONTEXT = "ctx"
    }

    @Test
    internal fun runScriptKt() {
        val ctx = JData(100, 20, JData())
        val apender: org.ujorm.kotlin.script.JAppender = ctx.appender
        apender.append('A')

        val engine = NashornScriptEngineFactory().getScriptEngine(SecureClassFilter())!!.apply {
            put(CONTEXT, ctx)
        }

        val script = getScript(ctx:: class)
        engine.eval(script)
        expect(ctx.appender.toString()).toEqual("AB400")
    }

    @Test
    internal fun runScriptEquals() {
        val ctx = JData(100, 20, JData())
        val apender: org.ujorm.kotlin.script.JAppender = ctx.appender
        apender.append('A')

        val engine = NashornScriptEngineFactory().getScriptEngine(SecureClassFilter())!!.apply {
            put(CONTEXT, ctx)
        }

        val script = getScript(ctx:: class)
        engine.eval(script)
        expect(ctx.appender.toString()).toEqual("AB400")
    }

    @Test
    internal fun runScriptKtForbidden() {
        val ctx = JData(100, 20, JData())
        val apender: org.ujorm.kotlin.script.JAppender = ctx.appender
        apender.append('A')

        val engine = NashornScriptEngineFactory().getScriptEngine(SecureClassFilter())!!.apply {
            put(CONTEXT, ctx)
        }

        expect {
            engine.eval("var file = new java.io.File(\"/\")")
        }.toThrow<RuntimeException>()
            .message.toContain("ClassNotFoundException: java.io.File")
    }
}

class JData (
    val i: Int = 400,
    val j: Int = 555,
    val data: JData? = null,
    val appender: JAppender = JAppender()
) {
    override fun toString(): String {
        return "JData(i=$i, j=$j)"
    }
}

class JAppender {
    val a = StringBuilder()

    fun append(a : Any?) {
        this.a.append(a)
    }

    override fun toString(): String {
        return a.toString()
    }
}

private fun getScript(contextType: KClass<*>) = arrayOf(
    "var appender = ctx.appender;",
    "appender.append('B')",
    "appender.append(ctx.data.i)",
    "if (appender.toString() != \"AB400\") throw new IllegalArgumentException(\"Wrong comparator\")",
).joinToString(separator = "\n")


// How to use?
class SecureClassFilter : ClassFilter {
    override fun exposeToScripts(className: String): Boolean = when (className) {
        File::class.java.name -> false
        else -> true
    }
}

