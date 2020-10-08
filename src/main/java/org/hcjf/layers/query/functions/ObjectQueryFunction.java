package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.math.BigDecimal;
import java.util.*;

public class ObjectQueryFunction extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String IS_NOT_NULL = "isNotNull";
    private static final String IS_NULL = "isNull";
    private static final String IS_COLLECTION = "isCollection";
    private static final String IS_MAP = "isMap";
    private static final String IS_DATE = "isDate";
    private static final String IS_STRING = "isString";
    private static final String IS_NUMBER = "isNumber";
    private static final String INSTANCE_OF = "instanceOf";
    private static final String IF = "if";
    private static final String CASE = "case";
    private static final String EQUALS = "equals";
    private static final String NEW = "new";
    private static final String NEW_MAP = "newMap";
    private static final String NEW_ARRAY = "newArray";

    private static final class InstanceOfValues {
        private static final String NULL = "NULL";
        private static final String COLLECTION = "COLLECTION";
        private static final String MAP = "MAP";
        private static final String DATE = "DATE";
        private static final String STRING = "STRING";
        private static final String NUMBER = "NUMBER";
        private static final String UUID = "UUID";
        private static final String BOOLEAN = "BOOLEAN";
        private static final String OBJECT = "OBJECT";
    }

    public ObjectQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.OBJECT_FUNCTION_NAME));

        addFunctionName(IS_NOT_NULL);
        addFunctionName(IS_NULL);
        addFunctionName(IS_COLLECTION);
        addFunctionName(IS_MAP);
        addFunctionName(IS_DATE);
        addFunctionName(IS_STRING);
        addFunctionName(IS_NUMBER);
        addFunctionName(INSTANCE_OF);
        addFunctionName(IF);
        addFunctionName(CASE);
        addFunctionName(EQUALS);
        addFunctionName(NEW);
        addFunctionName(NEW_MAP);
        addFunctionName(NEW_ARRAY);
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
            case(INSTANCE_OF): {
                Object parameter = null;
                if(parameters.length == 1) {
                    parameter = parameters[0];
                }
                if(parameter == null) {
                    result = InstanceOfValues.NULL;
                } else if(parameter instanceof Collection) {
                    result = InstanceOfValues.COLLECTION;
                } else if(parameter instanceof Map) {
                    result = InstanceOfValues.MAP;
                } else if(parameter instanceof Date) {
                    result = InstanceOfValues.DATE;
                } else if(parameter instanceof String) {
                    result = InstanceOfValues.STRING;
                } else if(parameter instanceof Number) {
                    result = InstanceOfValues.NUMBER;
                } else if(parameter instanceof UUID) {
                    result = InstanceOfValues.UUID;
                } else if(parameter instanceof Boolean) {
                    result = InstanceOfValues.BOOLEAN;
                } else {
                    result = InstanceOfValues.OBJECT;
                }
                break;
            }
            case(EQUALS): {
                Object parameter1 = getParameter(0, parameters);
                Object parameter2 = getParameter(1, parameters);

                if(parameter1 instanceof Number && parameter2 instanceof Number) {
                    BigDecimal bigDecimal1 = BigDecimal.valueOf(((Number) parameter1).doubleValue());
                    BigDecimal bigDecimal2 = BigDecimal.valueOf(((Number) parameter2).doubleValue());
                    result = bigDecimal1.equals(bigDecimal2);
                } else {
                    result = Objects.equals(parameter1, parameter2);
                }
                break;
            }
            case(NEW): {
                if(parameters.length == 1) {
                    result = getParameter(0, parameters);
                } else {
                    result = null;
                }
                break;
            }
            case(NEW_MAP): {
                Map<String,Object> map = new HashMap<>();
                for (int i = 0; i < parameters.length; i+=2) {
                    String key = parameters[i].toString();
                    Object value = null;
                    if(parameters.length > i+1) {
                        value = parameters[i+1];
                    }
                    map.put(key, value);
                }
                result = map;
                break;
            }
            case(NEW_ARRAY): {
                Collection collection = new ArrayList();
                for(Object parameter : parameters) {
                    collection.add(parameter);
                }
                result = collection;
                break;
            }
        }
        return result;
    }
}
