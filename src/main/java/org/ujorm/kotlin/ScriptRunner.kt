package org.ujorm.kotlin

import javax.script.ScriptEngineManager
import javax.script.ScriptException

object ScriptRunner {
    private const val ENGINE_NAME = "kts"
    private const val ARGUMENTS = "args"

    @Throws(ScriptException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Enter a Kotlin script with arguments")
        } else {
            val engine = ScriptEngineManager()
                .engineFactories
                .stream()
                .filter { it.extensions.contains(ENGINE_NAME) }
                .findFirst()
                .map { it.scriptEngine }
                .get()

            val statement = args.first()
            val arguments = args.drop(1).toTypedArray()

            engine.put(ARGUMENTS, arguments)
            val argsGetter = "val args = bindings.get(\"$ARGUMENTS\")" +
                    " as ${arguments::class.qualifiedName}<String>"
            engine.eval(argsGetter)
            val result = engine.eval(statement)
            println(">>> Script: $statement")
            println(">>> Result: $result")
        }
    }
}