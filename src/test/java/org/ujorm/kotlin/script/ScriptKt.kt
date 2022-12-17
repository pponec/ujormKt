package org.ujorm.kotlin.script

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test
import java.io.File
import javax.script.ScriptEngineManager

internal class ScriptTests {

    @Test
    internal fun runScript1() {
        val f = File("test22.txt")

        val engine = ScriptEngineManager().getEngineByExtension("kts")

        var e1 = "${f.getAbsolutePath()}" as Any?
        var e2 = engine.eval("\"${f.getAbsolutePath()}\"")

        expect(e1).toEqual(e2)
    }

    @Test
    internal fun runScript2() {

        val data = Data(10, 20, Data())

        val engine = ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            put("data", data)
        }

        var e1 = "${data.i}" as Any?
        var e2 = engine.eval("\"${data.i}\"")

        expect(e1).toEqual(e2)
    }

    /** Fails due */
    @Test
    internal fun runScript3() {
        val data = Data(100, 20, Data())

        val engine = ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            put("data", data)
        }
        data.appender.append('A')
        var result = engine.eval(getScript())
        expect(data.appender.toString()).toEqual("AB")
    }

    private fun getScript() = """
        data.appender.append('B');
    """.trimIndent()
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


