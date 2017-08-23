package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
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

    private final Set<String> aliases;

    public StringQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) +
                SystemProperties.get(SystemProperties.Query.Function.STRING_LAYER_NAME));

        aliases = new HashSet<>();
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + TRIM);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + LENGTH);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + SPLIT);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + CONCAT);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + STRING_JOIN);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + TO_UPPER_CASE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + TO_LOWER_CASE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + TO_STRING);
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
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
            case TO_STRING: checkSize(1, parameters)[0].toString();
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
