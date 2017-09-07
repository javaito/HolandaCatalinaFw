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
    private static final String CONCAT = "concat";
    private static final String STRING_JOIN = "stringJoin";
    private static final String TO_UPPER_CASE = "toUpperCase";
    private static final String TO_LOWER_CASE = "toLowerCase";
    private static final String TO_STRING = "toString";
    private static final String HEX_TO_BYTES = "hexToBytes";
    private static final String BYTES_TO_HEX = "bytesToHex";

    public StringQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.STRING_FUNCTION_NAME));

        addFunctionName(TRIM);
        addFunctionName(LENGTH);
        addFunctionName(SPLIT);
        addFunctionName(CONCAT);
        addFunctionName(STRING_JOIN);
        addFunctionName(TO_UPPER_CASE);
        addFunctionName(TO_LOWER_CASE);
        addFunctionName(TO_STRING);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;
        switch (functionName) {
            case TRIM: result = ((String)checkSize(1, parameters)[0]).trim();break;
            case LENGTH: result = ((String)checkSize(1, parameters)[0]).length();break;
            case SPLIT: result = ((String)checkSize(2, parameters)[0]).split((String)parameters[1]);break;
            case TO_UPPER_CASE: result = ((String)checkSize(1, parameters)[0]).toUpperCase();break;
            case TO_LOWER_CASE: result = ((String)checkSize(1, parameters)[0]).toLowerCase();break;
            case TO_STRING: result = checkSize(1, parameters)[0].toString(); break;
            case HEX_TO_BYTES: result = Strings.hexToBytes((String) checkSize(1, parameters)[0]);break;
            case BYTES_TO_HEX: result = Strings.bytesToHex((byte[]) checkSize(1, parameters)[0]);break;
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
                            builder.append(collectionParameter, (String)parameters[0]);
                        }
                    } else {
                        builder.append(parameters[i], (String)parameters[0]);
                    }
                }
                result = builder.toString();
            }
        }
        return result;
    }
}
