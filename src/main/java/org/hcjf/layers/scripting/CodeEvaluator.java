package org.hcjf.layers.scripting;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

/**
 * This interface provides a method to evaluate an script in different languages.
 */
public interface CodeEvaluator extends LayerInterface {

    /**
     * Evaluate the script with a set of parameter and store the result into the result object.
     * @param script Script to evaluate.
     * @param parameters Parameters object.
     * @return Returns a map with the result model of evaluate script.
     */
    ExecutionResult evaluate(String script, Map<String,Object> parameters);

}
