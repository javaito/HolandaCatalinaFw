package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.util.Collection;

/**
 * @author javaito
 */
public class StringQueryFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String TRIM = "trim";
    private static final String LENGTH = "length";
    private static final String SPLIT = "split";
    private static final String SPLIT_BY_LENGTH = "splitByLength";
    private static final String CONCAT = "concat";
    private static final String STRING_JOIN = "stringJoin";
    private static final String TO_UPPER_CASE = "toUpperCase";
    private static final String TO_LOWER_CASE = "toLowerCase";
    private static final String TO_STRING = "toString";
    private static final String HEX_TO_BYTES = "hexToBytes";
    private static final String BYTES_TO_HEX = "bytesToHex";
    private static final String REPLACE = "replace";

    public StringQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.STRING_FUNCTION_NAME));

        addFunctionName(TRIM);
        addFunctionName(LENGTH);
        addFunctionName(SPLIT);
        addFunctionName(SPLIT_BY_LENGTH);
        addFunctionName(CONCAT);
        addFunctionName(STRING_JOIN);
        addFunctionName(TO_UPPER_CASE);
        addFunctionName(TO_LOWER_CASE);
        addFunctionName(TO_STRING);
        addFunctionName(REPLACE);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;
        switch (functionName) {
            case TRIM: result = ((String)getParameter(0, parameters)).trim();break;
            case LENGTH: result = ((String)getParameter(0, parameters)).length();break;
            case SPLIT: result = ((String)getParameter(0, parameters)).split(getParameter(1, parameters));break;
            case SPLIT_BY_LENGTH: result = Strings.splitByLength(getParameter(0, parameters), getParameter(1, parameters));break;
            case TO_UPPER_CASE: result = ((String)getParameter(0, parameters)).toUpperCase();break;
            case TO_LOWER_CASE: result = ((String)getParameter(0, parameters)).toLowerCase();break;
            case TO_STRING: result = getParameter(0, parameters).toString(); break;
            case HEX_TO_BYTES: result = Strings.hexToBytes(getParameter(0, parameters));break;
            case BYTES_TO_HEX: result = Strings.bytesToHex(getParameter(0, parameters));break;
            case REPLACE: {
                String source = getParameter(0, parameters);
                String target = getParameter(1, parameters);
                String replacement = getParameter(2, parameters);
                result = source.replace(target, replacement);
                break;
            }
            case CONCAT: {
                StringBuilder builder = new StringBuilder();
                for(Object parameter : parameters) {
                    if(parameter instanceof Collection) {
                        for(Object collectionParameter : ((Collection)parameter)) {
                            builder.append(collectionParameter);
                        }
                    } else {
                        builder.append(parameter);
                    }
                }
                result = builder.toString();
                break;
            }
            case STRING_JOIN: {
                Strings.Builder builder = new Strings.Builder();
                for (int i = 1; i < parameters.length; i++) {
                    if(parameters[i] instanceof Collection) {
                        for(Object collectionParameter : ((Collection)parameters[i])) {
                            builder.append(collectionParameter, getParameter(0, parameters));
                        }
                    } else {
                        builder.append(parameters[i], (String)getParameter(0, parameters));
                    }
                }
                result = builder.toString();
            }
        }
        return result;
    }
}
