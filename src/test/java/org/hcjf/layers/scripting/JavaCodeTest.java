package org.hcjf.layers.scripting;

import org.hcjf.layers.Layers;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Introspection;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaCodeTest {

    @Test
    public void test() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "java");
        String script = "" +
                "System.out.printf(\"Esto tiene el campo 2 '%s', y esta es la iteración '%d'\", parameters.get(\"field2\"), parameters.get(\"iteration\"));" +
                "result.put(\"date\", new Date());";

        Long totalTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            int index = i;
            Service.run(() -> {
                Map<String,Object> parameters = new HashMap<>();
                parameters.put("field1", 34);
                parameters.put("field2", "Hola mundo!");
                parameters.put("iteration", index);
                Long time = System.currentTimeMillis();
                ExecutionResult executionResult = codeEvaluator.evaluate(script, parameters);
                Map<String,Object> result = executionResult.getResult();
                System.out.println(result.get("date"));
                System.out.println(result.get("_out"));
                System.out.println(result.get("_error"));
                System.out.println("Eval time: " + result.get("_evalTime"));
                System.out.println("Waiting vm time: " + result.get("_waitingVmTime"));
                System.out.println("Total time: " + (System.currentTimeMillis() - time));
                System.out.println();
                System.out.println("**************************");
                System.out.println();
            }, ServiceSession.getSystemSession(), true, Long.MAX_VALUE);
        }
        System.out.println("Total time: " + (System.currentTimeMillis() - totalTime));
    }

    @Test
    public void testAsync() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "java");
        String script = "" +
                "System.out.printf(\"Esto tiene el campo 2 '%s', y esta es la iteración '%d'\", parameters.get(\"field2\"), parameters.get(\"iteration\"));" +
                "result.put(\"date\", new Date());";
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            int index = i;
            Service.run(() -> {
                Map<String,Object> parameters = new HashMap<>();
                parameters.put("field1", 34);
                parameters.put("field2", "Hola mundo!");
                parameters.put("iteration", index);
                Long time = System.currentTimeMillis();
                ExecutionResult executionResult = codeEvaluator.evaluate(script, parameters);
                Map<String,Object> result = executionResult.getResult();
                System.out.println(result.get("date"));
                System.out.println(result.get("_out"));
                System.out.println(result.get("_error"));
                System.out.println("Eval time: " + result.get("_evalTime"));
                System.out.println("Waiting vm time: " + result.get("_waitingVmTime"));
                System.out.println("Total time: " + (System.currentTimeMillis() - time));
                System.out.println();
                System.out.println("**************************");
                counter.addAndGet(1);
            }, ServiceSession.getSystemSession());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(counter.get(), 10);
    }

    @Test(timeout = 10000)
    public void testTimeout() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "java");
        String script = "Thread.sleep(20000);";
        Map<String,Object> parameters = new HashMap<>();
        try {
            ExecutionResult executionResult = codeEvaluator.evaluate(script, parameters);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testInvalidCode() {
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "java");
        String script = "asdfasdfasdf";
        Map<String,Object> parameters = new HashMap<>();
        ExecutionResult executionResult = codeEvaluator.evaluate(script, parameters);
        if(executionResult.isExecutionFailed()) {
            List<String> diagnostics = Introspection.resolve(executionResult.getResult(), "_diagnostics");
            diagnostics.forEach(System.out::println);
            Assert.assertTrue(true);
        } else {
            Assert.fail();
        }
    }

    @Test
    public void testMaps() {
        String script =
                "Map<String,Object> agent = Introspection.resolve(assignment, \"agent\");" +
                "Number value = Introspection.resolve(agent, \"value\");" +
                "Double cost = -1 * value.doubleValue();" +
                "result.put(\"cost\", cost);";
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> assignment = new HashMap<>();
        Map<String, Object> agent = new HashMap<>();
        agent.put("value", 1);
        assignment.put("agent", agent);
        parameters.put("assignment", assignment);
        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, "java");
        parameters = codeEvaluator.evaluate(script, parameters).getResult();
        System.out.println(Introspection.resolve(parameters, "cost").toString());
    }

}
