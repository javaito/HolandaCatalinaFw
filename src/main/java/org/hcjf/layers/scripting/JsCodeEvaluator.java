package org.hcjf.layers.scripting;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.util.*;

public class JsCodeEvaluator extends Layer implements CodeEvaluator{

    private static final String METHOD_DEFINITION = "(%s) => {%s}";
    private static final String GRAALVM_JS = "js";
    private final Context context;

    public JsCodeEvaluator() {
        context = Context.newBuilder(GRAALVM_JS).build();
    }

    @Override
    public String getImplName() {
        return SystemProperties.get(SystemProperties.CodeEvaluator.Js.IMPL_NAME);
    }

    /**
     * Evaluate the script with a set of parameter and store the result into the result object.
     * @param script     Script to evaluate.
     * @param parameters Parameters object.
     * @return Returns a map with the result model of evaluate script.
     */
    @Override
    public ExecutionResult evaluate(String script, Map<String, Object> parameters) {

        Strings.Builder parametersBuilder = new Strings.Builder();
        List<Object> values = new ArrayList<>();
        for(String parameterName : parameters.keySet()) {
            parametersBuilder.append(parameterName, Strings.ARGUMENT_SEPARATOR);
            values.add(getParameterInstance(parameterName, parameters));
        }
        String methodDefinition = String.format(METHOD_DEFINITION, parametersBuilder, script);
        Value value = context.eval(GRAALVM_JS, methodDefinition);
        ExecutionResult executionResult = new ExecutionResult(
                ExecutionResult.State.SUCCESS,
                Map.of(),
                parameters, getResultInstance(value.execute(values.toArray())));
        return executionResult;
    }

    /**
     * Returns the correct instance to bind the values with the script.
     * @param parameterName Name of the parameter.
     * @param parameters Parameters map.
     * @return Returns the parameter instance.
     */
    public Object getParameterInstance(String parameterName, Map<String,Object> parameters) {
        Object result = parameters.get(parameterName);
        if(result instanceof Map) {
            result = ProxyObject.fromMap((Map<String, Object>) result);
        } else if(result instanceof Collection) {
            result = ProxyArray.fromList(Arrays.asList(((Collection)result).toArray()));
        }
        return result;
    }

    /**
     * Returns the correct instance to bind the script result with the value of java dom.
     * @param scriptResult Script result instance.
     * @return Binding instance.
     */
    public Object getResultInstance(Object scriptResult) {
        Object result = scriptResult;
        if(scriptResult instanceof Value) {
            result = getResultInstance((Value) result);
        }
        return result;
    }

    /**
     * Returns the correct instance to bind the script result with the value of java dom.
     * @param scriptResult Script result instance.
     * @return Binding instance.
     */
    public Object getResultInstance(Value scriptResult) {
        Object result = null;
        if(scriptResult.isBoolean()) {
            result = scriptResult.asBoolean();
        } else if(scriptResult.isDate()) {
            result = scriptResult.asDate();
        } else if(scriptResult.isString()) {
            result = scriptResult.asString();
        } else if(scriptResult.isNumber()) {
            result = scriptResult.asDouble();
        } else if(scriptResult.isProxyObject()) {
            ProxyObject proxyObject = scriptResult.asProxyObject();
            Map<String,Object> resultMap = new HashMap<>();
            ProxyArray list = (ProxyArray) proxyObject.getMemberKeys();
            for (long i = 0; i < list.getSize(); i++) {
                String key = (String) list.get(i);
                resultMap.put(key, getResultInstance(resultMap.get(key)));
            }
            result = resultMap;
        } else {
            Map<String,Object> resultMap = new HashMap<>();
            for(String key : scriptResult.getMemberKeys()) {
                resultMap.put(key, getResultInstance(scriptResult.getMember(key)));
            }
            result = resultMap;
        }
        return result;
    }
}
