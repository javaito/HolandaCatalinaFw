package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ObjectQueryFunction extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String IS_NOT_NULL = "isNotNull";
    private static final String IS_NULL = "isNull";
    private static final String IS_COLLECTION = "isCollection";
    private static final String IS_MAP = "isMap";
    private static final String IS_DATE = "isDate";
    private static final String IS_STRING = "isString";
    private static final String IS_NUMBER = "isNumber";
    private static final String IF = "if";
    private static final String CASE = "case";
    private static final String EQUALS = "equals";
    private static final String NEW = "new";

    public ObjectQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.OBJECT_FUNCTION_NAME));

        addFunctionName(IS_NOT_NULL);
        addFunctionName(IS_NULL);
        addFunctionName(IS_COLLECTION);
        addFunctionName(IS_MAP);
        addFunctionName(IS_DATE);
        addFunctionName(IS_STRING);
        addFunctionName(IS_NUMBER);
        addFunctionName(IF);
        addFunctionName(CASE);
        addFunctionName(EQUALS);
        addFunctionName(NEW);
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
            case(IS_NULL): {
                boolean booleanValue = false;
                for(Object parameter : parameters) {
                    booleanValue = parameter == null;
                    if(booleanValue) {
                        break;
                    }
                }
                result = booleanValue;
                break;
            }
            case(IF): {
                Boolean condition = getParameter(0, parameters);
                if(condition != null && condition) {
                    result = getParameter(1, parameters);
                } else {
                    if(parameters.length == 3) {
                        result = getParameter(2, parameters);
                    }
                }
                break;
            }
            case(CASE): {
                Object mainValue = getParameter(0, parameters);
                for (int i = 1; i < parameters.length; i += 2) {
                    if(i + 1 < parameters.length) {
                        if(mainValue.equals(parameters[i])) {
                            result = parameters[i+1];
                            break;
                        }
                    } else {
                        result = parameters[i];
                    }
                }
                break;
            }
            case(IS_COLLECTION): {
                result = getParameter(0, parameters) != null && getParameter(0, parameters) instanceof Collection;
                break;
            }
            case(IS_MAP): {
                result = getParameter(0, parameters) != null && getParameter(0, parameters) instanceof Map;
                break;
            }
            case(IS_DATE): {
                result = getParameter(0, parameters) != null && getParameter(0, parameters) instanceof Date;
                break;
            }
            case(IS_STRING): {
                result = getParameter(0, parameters) != null && getParameter(0, parameters) instanceof String;
                break;
            }
            case(IS_NUMBER): {
                result = getParameter(0, parameters) != null && getParameter(0, parameters) instanceof Number;
                break;
            }
            case(EQUALS): {
                result = Objects.equals(getParameter(0, parameters), getParameter(1, parameters));
                break;
            }
            case(NEW): {
                result = getParameter(0, parameters);
                break;
            }
        }
        return result;
    }
}
