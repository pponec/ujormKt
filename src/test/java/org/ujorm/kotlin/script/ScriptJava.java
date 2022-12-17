package org.ujorm.kotlin.script;

import org.junit.jupiter.api.Test;

import javax.script.*;
import ch.tutteli.atrium.api.fluent.en_GB.*;
import ch.tutteli.atrium.api.verbs.*;
import org.junit.jupiter.api.Test;
import javax.script.ScriptEngineManager;
import java.io.File;

import static ch.tutteli.atrium.api.verbs.ExpectKt.expect;

public class ScriptJava {



    @Test
    public void test3() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        // create File object
        var f = new StringBuilder();
        f.append('A');
        // expose File object as a global variable to the engine
        engine.put("f", f);
        // evaluate JavaScript code and access the variable
        engine.eval("f.append('B');");

        System.out.println("...." + f.toString());
    }

    @Test
    public void test2() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        // create File object
        File f = new File("test.txt");
        // expose File object as a global variable to the engine
        engine.put("file", f);
        // evaluate JavaScript code and access the variable
        engine.eval("print(file.getAbsolutePath())");

        System.out.println("....");
    }

    @Test
    public void testEval() throws Exception {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("nashorn");
//        engine.put("data", new org.ujorm.kotlin.script.Data());
//
//
//        // evaluate JavaScript code
//        var result = engine.eval("data.text");
//
//        System.out.println(result);
//        assert "X".equals(String.valueOf(result)): "error";
        // ch.tutteli.atrium.api.verbs.ExpectKt.expect(result).toEqual(7)
    }

    class Data {
        public int i = 10;
        public int j = 20;
        public String text = "TEXT";
    }

}
