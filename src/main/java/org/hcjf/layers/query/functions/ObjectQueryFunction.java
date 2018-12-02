package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ObjectQueryFunction extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String IS_NOT_NULL = "isNotNull";
    private static final String IS_COLLECTION = "isCollection";
    private static final String IS_MAP = "isMap";
    private static final String IS_DATE = "isDate";
    private static final String IS_STRING = "isString";
    private static final String IS_NUMBER = "isNumber";
    private static final String IF = "if";
    private static final String EQUALS = "equals";

    public ObjectQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.OBJECT_FUNCTION_NAME));

        addFunctionName(IS_NOT_NULL);
        addFunctionName(IS_COLLECTION);
        addFunctionName(IS_MAP);
        addFunctionName(IS_DATE);
        addFunctionName(IS_STRING);
        addFunctionName(IS_NUMBER);
        addFunctionName(IF);
        addFunctionName(EQUALS);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;
        switch(functionName) {
            case(IS_NOT_NULL): {
                boolean booleanValue = false;
                for(Object parameter : parameters) {
                    booleanValue = parameter != null;
                    if(!booleanValue) {
                        break;
                    }
                }
                result = booleanValue;
                break;
            }
            case(IF): {
                Boolean condition;
                Object ifValue;
                Object elseValue = null;
                try {
                    condition = (Boolean) checkSize(3, parameters)[0];
                    ifValue = parameters[1];
                    elseValue = parameters[2];
                } catch (Exception ex) {
                    condition = (Boolean) checkSize(2, parameters)[0];
                    ifValue = parameters[1];
                }
                if(condition) {
                    result = ifValue;
                } else {
                    result = elseValue;
                }
                break;
            }
            case(IS_COLLECTION): {
                result = checkSize(1, parameters)[0] != null && parameters[0] instanceof Collection;
                break;
            }
            case(IS_MAP): {
                result = checkSize(1, parameters)[0] != null && parameters[0] instanceof Map;
                break;
            }
            case(IS_DATE): {
                result = checkSize(1, parameters)[0] != null && parameters[0] instanceof Date;
                break;
            }
            case(IS_STRING): {
                result = checkSize(1, parameters)[0] != null && parameters[0] instanceof String;
                break;
            }
            case(IS_NUMBER): {
                result = checkSize(1, parameters)[0] != null && parameters[0] instanceof Number;
                break;
            }
            case(EQUALS): {
                result = Objects.equals(checkSize(2, parameters)[0], parameters[1]);
                break;
            }
        }
        return result;
    }
}
