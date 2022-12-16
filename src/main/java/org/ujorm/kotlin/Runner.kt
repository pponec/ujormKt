package org.ujorm.kotlin

import javax.script.ScriptEngineManager
import javax.script.ScriptException

object Runner {
    private const val ENGINE_NAME = "kts"

    @Throws(ScriptException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Hello Word!")
        } else {
            val engine = ScriptEngineManager().getEngineByName(ENGINE_NAME)
            if (engine == null) {
                println("Engine '${ENGINE_NAME}' was not found")

                val stringBuilder = StringBuilder()
                stringBuilder.append("ABC")

                ScriptEngineManager().engineFactories.forEach {
                    println(">>> ${it.extensions}")
                    if (it.extensions.contains("kts")) {
                        println(">>> Extension: ${it.extensions}")

                        it.scriptEngine.put("stringBuilder", stringBuilder)
                        try {
                            var result = it.scriptEngine.eval("100 + 20L")
                            println(">>> Result: $result")
                            var result2 = it.scriptEngine.eval("stringBuilder.append(\"ABC\")")
                            println(">>> stringBuilder: $stringBuilder")
                        } catch(e: Exception) {
                            e.printStackTrace()
                            return
                        }
                    }
                }
                return
            }
            println("Engine is OK")
            val statement = java.lang.String.join(" ", *args)
            println("Run: $statement")
            // expose File object as a global variable to the engine
            //engine.put("args", args);
            // evaluate JavaScript code and access the variable

            try {
                val result = engine.eval(statement)
                println(">>> Result: $result")
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}