package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRemoteException;
import org.hcjf.layers.Layers;
import org.hcjf.layers.scripting.CodeEvaluator;
import org.hcjf.layers.scripting.ExecutionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShellQueryFunction extends BaseQueryFunctionLayer {

    private static final String PARAMETER_PATTERN = "_p%d";
    private static final String STATEMENT_PATTERN = "result.put(\"_r\", %s);";
    private static final String RESULT_VAR = "_r";

    private static final class Functions {
        private static final String JAVA = "java";
    }

    private static final String NAME = "shell";

    public ShellQueryFunction() {
        super(NAME);
        addFunctionName(Functions.JAVA);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        if(parameters.length == 0) {
            throw new HCJFRemoteException("The shell function expected at list one parameter, the script to evaluate");
        }
        if(!(parameters[0] instanceof String)) {
            throw new HCJFRemoteException("The shell function expected the first parameter as script to evaluate");
        }
        String script = (String) parameters[0];
        Map<String,Object> parametersMap = new HashMap<>();
        for (int i = 1; i < parameters.length; i++) {
            Object parameter = parameters[i];
            if(parameter instanceof Map) {
                parametersMap.putAll((Map)parameter);
            } else {
                parametersMap.put(String.format(PARAMETER_PATTERN, i), parameter);
            }
        }

        Collection<String> keys = new ArrayList<>(parametersMap.keySet());
        for(String key : keys) {
            if(parametersMap.get(key) instanceof Enum) {
                parametersMap.remove(key);
            }
        }

        CodeEvaluator codeEvaluator = Layers.get(CodeEvaluator.class, functionName);
        ExecutionResult result = codeEvaluator.evaluate(String.format(STATEMENT_PATTERN, script), parametersMap);
        return result.getResult().get(RESULT_VAR);
    }

}
