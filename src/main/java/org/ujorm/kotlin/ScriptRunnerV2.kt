package org.ujorm.kotlin

import javax.script.ScriptEngineManager
import javax.script.ScriptException

object ScriptRunnerV2 {
    private const val ENGINE_NAME = "kts"

    @Throws(ScriptException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Enter a Kotlin script")
        } else {
            val engine = ScriptEngineManager()
                .engineFactories
                .stream()
                .filter { it.extensions.contains(ENGINE_NAME) }
                .findFirst()
                .map { it.scriptEngine }
                .get()

            val statement = java.lang.String.join(" ", *args)

            val result = engine.eval(statement)
            println(">>> Script: $statement")
            println(">>> Result: $result")
        }
    }
}