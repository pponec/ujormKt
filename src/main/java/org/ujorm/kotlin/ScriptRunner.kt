package org.ujorm.kotlin

import org.openjdk.nashorn.api.scripting.ClassFilter
import javax.script.ScriptEngineManager
import javax.script.ScriptException

object ScriptRunner {
    private const val KOTLIN_NAME = "kotlin"
    private const val ARGUMENTS = "args"

    @Throws(ScriptException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Enter a Kotlin script with arguments")
        } else {
            val engine = ScriptEngineManager().getEngineByName(KOTLIN_NAME)
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

    // How to use?
    class SecureClassFilter: ClassFilter {
        override fun exposeToScripts(className: String?): Boolean {
            return false;
        }
    }
}