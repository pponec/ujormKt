package org.ujorm.kotlin.script

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.polyfills.fullName
import org.junit.jupiter.api.Test
import org.openjdk.nashorn.api.scripting.ClassFilter
import javax.script.ScriptEngineManager
import kotlin.reflect.KClass

internal class ScriptTests {

    companion object {
        private const val KOTLIN_NAME = "kotlin"
        private const val CONTEXT = "data"
    }

    @Test
    internal fun runScriptKt() {
        val ctx = Data(100, 20, Data())
        val apender: org.ujorm.kotlin.script.MyAppender = ctx.appender
        apender.append('A')

        val engine = ScriptEngineManager().getEngineByName(KOTLIN_NAME)!!.apply {
            put(CONTEXT, ctx)
        }

        val script = getScript(ctx:: class)
        engine.eval(script)
        expect(ctx.appender.toString()).toEqual("AB400")
    }

    private fun getScript(contextType: KClass<*>) = arrayOf(
        "var ctx = bindings.get(\"$CONTEXT\") as ${contextType.fullName}",
        "val appender = ctx.appender",
        "appender.append('B')",
        "appender.append(ctx.data?.i)",
    ).joinToString(separator = "\n")
}

class Data (
    val i: Int = 400,
    val j: Int = 555,
    val data: Data? = null,
    val appender: MyAppender = MyAppender()
) {
    override fun toString(): String {
        return "Data(i=$i, j=$j)"
    }
}

class MyAppender {
    val a = StringBuilder()

    fun append(a : Any?) {
        this.a.append(a)
    }

    override fun toString(): String {
        return a.toString()
    }
}

class SecureClassFilter: ClassFilter {

    override fun exposeToScripts(className: String?): Boolean {
        return true;
    }
}


