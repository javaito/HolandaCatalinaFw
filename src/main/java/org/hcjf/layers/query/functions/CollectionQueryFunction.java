package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 */
public class CollectionQueryFunction extends BaseQueryFunctionLayer {

    private static final String COUNT = "count";

    public CollectionQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.COLLECTION_FUNCTION_NAME));

        addFunctionName(COUNT);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;

        switch (functionName) {
            case COUNT: {
                if(checkSize(1, parameters)[0] instanceof Collection) {
                    result = ((Collection)parameters[0]).size();
                } else if(checkSize(1, parameters)[0] instanceof Map) {
                    result = ((Map)parameters[0]).size();
                } else if(checkSize(1, parameters)[0].getClass().isArray()) {
                    result = Array.getLength(parameters[0]);
                } else {
                    result = 1;
                }
            }
        }

        return result;
    }

}
