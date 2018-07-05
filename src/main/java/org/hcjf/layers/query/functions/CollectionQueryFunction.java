package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

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
                if(checkSize(1, parameters)[0] instanceof Collection) {
                    result = ((Collection)parameters[0]).size();
                } else if(checkSize(1, parameters)[0] instanceof Map) {
                    result = ((Map)parameters[0]).size();
                } else if(checkSize(1, parameters)[0].getClass().isArray()) {
                    result = Array.getLength(parameters[0]);
                } else {
                    result = 1;
                }
                break;
            }
            case GET: {
                if(checkSize(2, parameters)[0] instanceof Collection && parameters[1] instanceof Integer) {
                    result = Array.get(((Collection)parameters[0]).toArray(), (Integer)parameters[1]);
                } else if(checkSize(2, parameters)[0].getClass().isArray() && parameters[1] instanceof Integer) {
                    result = Array.get(parameters[0], (Integer)parameters[1]);
                } else if(checkSize(2, parameters)[0] instanceof Map) {
                    result = ((Map)parameters[0]).get(parameters[1]);
                }
                break;
            }
            case CONTAINS: {
                if(checkSize(2, parameters)[0] instanceof Collection) {
                    result = ((Collection) parameters[0]).contains(parameters[1]);
                } else if(checkSize(2, parameters)[0].getClass().isArray()) {
                    result = Arrays.asList(parameters[0]).contains(parameters[1]);
                } else {
                    throw new IllegalArgumentException("Contains functions is only for collections and arrays");
                }
                break;
            }
            case CONTAINS_ALL: {
                Collection firstCollection;
                Collection secondCollection;
                if(checkSize(2, parameters)[0] instanceof Collection) {
                    firstCollection = (Collection) parameters[0];
                } else if(parameters[0].getClass().isArray()) {
                    firstCollection = Arrays.asList(parameters[0]);
                } else {
                    throw new IllegalArgumentException("The first parameter for contains all function can only be a collection or an array");
                }

                if(parameters[1] instanceof Collection) {
                    secondCollection = (Collection) parameters[1];
                } else if(parameters[1].getClass().isArray()) {
                    secondCollection = Arrays.asList(parameters[1]);
                } else {
                    throw new IllegalArgumentException("The second parameter for contains all function can only be a collection or an array");
                }

                result = firstCollection.containsAll(secondCollection);
                break;
            }
            case CONTAINS_KEY: {
                if(checkSize(2, parameters)[0] instanceof Map) {
                    result = ((Map)parameters[0]).containsKey(parameters[1]);
                } else {
                    throw new IllegalArgumentException("Contains key function is only for maps");
                }
                break;
            }
            case CONTAINS_ALL_KEYS: {
                Map map;
                Collection collection;
                if(checkSize(2, parameters)[0] instanceof Map) {
                    map = (Map) parameters[0];
                } else {
                    throw new IllegalArgumentException("The first parameter for contains all keys function can only be a map");
                }

                if(parameters[1] instanceof Collection) {
                    collection = (Collection) parameters[1];
                } else if(parameters[1].getClass().isArray()) {
                    collection = Arrays.asList(parameters[1]);
                } else {
                    throw new IllegalArgumentException("The second parameter for contains all keys function can only be a collection or an array");
                }

                result = map.keySet().containsAll(collection);
                break;
            }
        }

        return result;
    }

}
