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


    @Test
    internal fun runScript3() {

        val stringBuilder2 = StringBuilder().append("")
        val data = Data(100, 20, Data())

        val engine = ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            put("stringBuilder2", stringBuilder2)
            put("data", data)
        }

        var result = engine.eval("stringBuilder2.append(\"XYZ_${data.i}\")")
        expect(result).toEqual("XYZ")
    }

}

class Data (
    val i: Int = 400,
    val j: Int = 555,
    val data: Data? = null,
) {
    override fun toString(): String {
        return "Data(i=$i, j=$j)"
    }
}



