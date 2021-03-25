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
            result = getProxyObjectFromMap((Map<String, Object>) result);
        } else if(result instanceof Collection) {
            result = getProxyArrayFromCollection((Collection<Object>) result);
        }
        return result;
    }

    public ProxyObject getProxyObjectFromMap(Map<String,Object> map) {
        Map<String,Object> mapCopy = new HashMap<>();
        for(String key : map.keySet()) {
            Object value = map.get(key);
            if(value instanceof Map) {
                mapCopy.put(key, getProxyObjectFromMap((Map<String, Object>) value));
            } else if(value instanceof Collection) {
                mapCopy.put(key, getProxyArrayFromCollection((Collection<Object>) value));
            } else {
                mapCopy.put(key, value);
            }
        }
        return ProxyObject.fromMap(mapCopy);
    }

    public ProxyArray getProxyArrayFromCollection(Collection<Object> collection) {
        List<Object> collectionCopy = new ArrayList<>();
        for(Object value : collection) {
            if(value instanceof Map) {
                collectionCopy.add(getProxyObjectFromMap((Map<String, Object>) value));
            } else if(value instanceof Collection) {
                collectionCopy.add(getProxyArrayFromCollection((Collection<Object>) value));
            } else {
                collectionCopy.add(value);
            }
        }
        return ProxyArray.fromList(collectionCopy);
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
        } else if(scriptResult instanceof ProxyObject) {
            result = getResultFromProxyObject((ProxyObject) result);
        } else if(scriptResult instanceof ProxyArray) {
            result = getResultFromProxyArray((ProxyArray) result);
        }
        return result;
    }

    public Object getResultFromProxyObject(ProxyObject proxyObject) {
        Map<String, Object> resultMap = new HashMap<>();
        ProxyArray list = (ProxyArray) proxyObject.getMemberKeys();
        for (long i = 0; i < list.getSize(); i++) {
            String key = (String) list.get(i);
            resultMap.put(key, getResultInstance(proxyObject.getMember(key)));
        }
        return resultMap;
    }

    public Object getResultFromProxyArray(ProxyArray proxyArray) {
        Collection<Object> resultArray = new ArrayList<>();
        for (long i = 0; i < proxyArray.getSize(); i++) {
            resultArray.add(getResultInstance(proxyArray.get(i)));
        }
        return resultArray;
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
            Object proxy = scriptResult.asProxyObject();
            if(proxy instanceof ProxyObject) {
                result = getResultFromProxyObject((ProxyObject) proxy);
            } else if(proxy instanceof ProxyArray) {
                result = getResultFromProxyArray((ProxyArray) proxy);
            }
        } else {
            if(scriptResult.hasArrayElements()) {
                Collection<Object> resultCollection = new ArrayList<>();
                for (long i = 0; i < scriptResult.getArraySize(); i++) {
                    resultCollection.add(getResultInstance(scriptResult.getArrayElement(i)));
                }
                result = resultCollection;
            } else {
                Map<String, Object> resultMap = new HashMap<>();
                for (String key : scriptResult.getMemberKeys()) {
                    resultMap.put(key, getResultInstance(scriptResult.getMember(key)));
                }
                result = resultMap;
            }
        }
        return result;
    }
}
