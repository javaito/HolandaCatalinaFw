package org.hcjf.layers.scripting;

import org.hcjf.layers.Layers;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsCodeTest {

    @Test
    public void test() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "js");
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("name", "javier");
        parameters.put("age", 40);
        parameters.put("date", new Date());
        parameters.put("map", Map.of("lastName", "quiroga"));

        String script = "" +
                "print(name);" +
                "print(age);" +
                "print(date);" +
                "print(map);" +
                "print(index);" +
                "return age * index;";

        Long time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            parameters.put("index", i);
            ExecutionResult result = codeEvaluator.evaluate(script, parameters);
            System.out.println(result.getResult().toString());
        }
        System.out.println("Time:" + (System.currentTimeMillis() - time));
    }

    @Test
    public void testReturnMap() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "js");
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("name", "javier");
        parameters.put("age", 40);
        parameters.put("date", new Date());
        parameters.put("map", Map.of("lastName", "quiroga"));

        String script =
                "print(name);" +
                "print(age);" +
                "print(date);" +
                "print(map);" +
                "return {key:\"value\"};";
        ExecutionResult result = codeEvaluator.evaluate(script, parameters);
        System.out.println(result.getResult().toString());
    }
}
