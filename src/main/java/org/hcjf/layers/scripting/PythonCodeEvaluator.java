package org.hcjf.layers.scripting;

import org.hcjf.layers.Layer;

import java.util.Map;

public class PythonCodeEvaluator extends Layer implements CodeEvaluator {

    /**
     * Evaluate the script with a set of parameter and store the result into the result object.
     * @param script     Script to evaluate.
     * @param parameters Parameters object.
     * @return Returns a map with the result model of evaluate script.
     */
    @Override
    public ExecutionResult evaluate(String script, Map<String, Object> parameters) {

//        ServiceSession.getCurrentIdentity().

        return null;
    }

}
