package org.ujorm.kotlin.script

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.polyfills.fullName
import org.junit.jupiter.api.Test
import javax.script.ScriptEngineManager

internal class ScriptTests {

    companion object {
        private const val KOTLIN_EXTENSION = "kts"
        private const val CONTEXT = "data"
    }

    /** Fails due: javax.script.ScriptException: Unresolved reference: data */
    @Test
    internal fun runScriptKt() {
        val ctx = Data(100, 20, Data())
        val apender: org.ujorm.kotlin.script.MyAppender = ctx.appender
        apender.append('A')

        val engine = ScriptEngineManager().getEngineByExtension(KOTLIN_EXTENSION)!!.apply {
            put(CONTEXT, ctx)
        }

        val script = getScript();
        engine.eval(script)
        expect(ctx.appender.toString()).toEqual("AB100")
    }

    private fun getScript() = arrayOf(
        "var ctx = bindings.get(\"$CONTEXT\") as ${Data::class.fullName}",
        "val appender = ctx.appender",
        "appender.append('B')",
        "appender.append(ctx.i)",
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

    fun append(a : Any) {
        this.a.append(a)
    }

    override fun toString(): String {
        return a.toString()
    }
}


