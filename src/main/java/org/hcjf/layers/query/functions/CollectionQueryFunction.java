package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 */
public class CollectionQueryFunction extends BaseQueryFunctionLayer {

    private static final String SIZE = "size";
    private static final String GET = "get";
    private static final String CONTAINS = "contains";
    private static final String CONTAINS_ALL = "containsAll";
    private static final String CONTAINS_KEY = "containsKey";
    private static final String CONTAINS_ALL_KEYS = "containsAllKeys";

    public CollectionQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.COLLECTION_FUNCTION_NAME));

        addFunctionName(SIZE);
        addFunctionName(GET);
        addFunctionName(CONTAINS);
        addFunctionName(CONTAINS_ALL);
        addFunctionName(CONTAINS_KEY);
        addFunctionName(CONTAINS_ALL_KEYS);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;

        switch (functionName) {
            case SIZE: {
                if(getParameter(0, parameters) instanceof Collection) {
                    result = ((Collection)parameters[0]).size();
                } else if(getParameter(0, parameters) instanceof Map) {
                    result = ((Map)parameters[0]).size();
                } else if(getParameter(0, parameters).getClass().isArray()) {
                    result = Array.getLength(parameters[0]);
                } else {
                    result = 1;
                }
                break;
            }
            case GET: {
                if(getParameter(0, parameters) instanceof Collection && parameters[1] instanceof Integer) {
                    result = Array.get(((Collection)parameters[0]).toArray(), (Integer)parameters[1]);
                } else if(getParameter(0, parameters).getClass().isArray() && parameters[1] instanceof Integer) {
                    result = Array.get(parameters[0], (Integer)parameters[1]);
                } else if(getParameter(0, parameters) instanceof Map) {
                    result = ((Map)parameters[0]).get(parameters[1]);
                }
                break;
            }
            case CONTAINS: {
                if(getParameter(0, parameters) instanceof Collection) {
                    result = ((Collection) getParameter(0, parameters)).contains(getParameter(1, parameters));
                } else if(getParameter(0, parameters).getClass().isArray()) {
                    result = Arrays.asList(getParameter(0, parameters)).contains(getParameter(1, parameters));
                } else {
                    throw new HCJFRuntimeException("Contains functions is only for collections and arrays");
                }
                break;
            }
            case CONTAINS_ALL: {
                Collection firstCollection;
                Collection secondCollection;
                if(getParameter(0, parameters) instanceof Collection) {
                    firstCollection = getParameter(0, parameters);
                } else if(getParameter(0, parameters).getClass().isArray()) {
                    firstCollection = Arrays.asList(getParameter(0, parameters));
                } else {
                    throw new HCJFRuntimeException("The first parameter for contains all function can only be a collection or an array");
                }

                if(getParameter(1, parameters) instanceof Collection) {
                    secondCollection = getParameter(1, parameters);
                } else if(getParameter(1, parameters).getClass().isArray()) {
                    secondCollection = Arrays.asList(getParameter(1, parameters));
                } else {
                    throw new HCJFRuntimeException("The second parameter for contains all function can only be a collection or an array");
                }

                result = firstCollection.containsAll(secondCollection);
                break;
            }
            case CONTAINS_KEY: {
                if(getParameter(0, parameters) instanceof Map) {
                    result = ((Map)getParameter(0, parameters)).containsKey(getParameter(1, parameters));
                } else {
                    throw new HCJFRuntimeException("Contains key function is only for maps");
                }
                break;
            }
            case CONTAINS_ALL_KEYS: {
                Map map;
                Collection collection;
                if(getParameter(0, parameters) instanceof Map) {
                    map = getParameter(0, parameters);
                } else {
                    throw new HCJFRuntimeException("The first parameter for contains all keys function can only be a map");
                }

                if(getParameter(1, parameters) instanceof Collection) {
                    collection = getParameter(1, parameters);
                } else if(parameters[1].getClass().isArray()) {
                    collection = Arrays.asList(getParameter(1, parameters));
                } else {
                    throw new HCJFRuntimeException("The second parameter for contains all keys function can only be a collection or an array");
                }

                result = map.keySet().containsAll(collection);
                break;
            }
        }

        return result;
    }

}
