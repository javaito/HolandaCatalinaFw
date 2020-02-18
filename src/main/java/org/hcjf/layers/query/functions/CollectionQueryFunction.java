package org.hcjf.layers.query.functions;

import io.kubernetes.client.proto.V1;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String KEYS = "keys";
    private static final String SORT = "sort";
    private static final String FIRST = "first";
    private static final String LAST = "last";
    private static final String SKIP = "skip";
    private static final String LIMIT = "limit";

    public CollectionQueryFunction() {
        super(SystemProperties.get(SystemProperties.Query.Function.COLLECTION_FUNCTION_NAME));

        addFunctionName(SIZE);
        addFunctionName(GET);
        addFunctionName(CONTAINS);
        addFunctionName(CONTAINS_ALL);
        addFunctionName(CONTAINS_KEY);
        addFunctionName(CONTAINS_ALL_KEYS);
        addFunctionName(KEYS);
        addFunctionName(SORT);
        addFunctionName(FIRST);
        addFunctionName(LAST);
        addFunctionName(SKIP);
        addFunctionName(LIMIT);
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
                if(getParameter(0, parameters) instanceof Collection) {
                    result = Array.get(((Collection)parameters[0]).toArray(), ((Number)parameters[1]).intValue());
                } else if(getParameter(0, parameters).getClass().isArray()) {
                    result = Array.get(parameters[0], ((Number)parameters[1]).intValue());
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
            case KEYS: {
                Map map;
                if(getParameter(0, parameters) instanceof Map) {
                    map = getParameter(0, parameters);
                } else {
                    throw new HCJFRuntimeException("The first parameter for 'keys' function can only be a map");
                }

                result = map.keySet();
                break;
            }
            case SORT: {
                Set<Object> set = new TreeSet<>((comparable1, comparable2) -> {
                    int compareResult;
                    if (comparable1 == null ^ comparable2 == null) {
                        compareResult = (comparable1 == null) ? -1 : 1;
                    } else if (comparable1 == null && comparable2 == null) {
                        compareResult = 0;
                    } else {
                        if(comparable1 instanceof Comparable && comparable2 instanceof Comparable) {
                            compareResult = ((Comparable)comparable1).compareTo(comparable2);
                        } else {
                            compareResult = comparable1.hashCode() - comparable2.hashCode();
                        }
                    }
                    return compareResult;
                });
                if(getParameter(0, parameters) instanceof  Collection) {
                    set.addAll(getParameter(0, parameters));
                } else {
                    set.add(getParameter(0, parameters));
                }
                result = set;
                break;
            }
            case FIRST: {
                if(getParameter(0, parameters) instanceof  Collection) {
                    result = ((Collection)getParameter(0, parameters)).stream().findFirst().orElse(null);
                } else {
                    result = getParameter(0, parameters);
                }
                break;
            }
            case LAST: {
                if(getParameter(0, parameters) instanceof  Collection) {
                    Collection collection = getParameter(0, parameters);
                    if(collection.size() > 0) {
                        result = ((Collection) getParameter(0, parameters)).stream().skip(collection.size() - 1).findFirst().orElse(null);
                    } else {
                        result = null;
                    }
                } else {
                    result = getParameter(0, parameters);
                }
                break;
            }
            case SKIP: {
                if(getParameter(0, parameters) instanceof  Collection) {
                    result = ((Collection)getParameter(0, parameters)).stream().skip(
                            ((Number)getParameter(1, parameters)).longValue()).collect(Collectors.toList());
                } else {
                    result = new ArrayList<>();
                }
                break;
            }
            case LIMIT : {
                if(getParameter(0, parameters) instanceof  Collection) {
                    result = ((Collection)getParameter(0, parameters)).stream().limit(
                            ((Number)getParameter(1, parameters)).longValue()).collect(Collectors.toList());
                } else {
                    List<Object> list = new ArrayList<>();
                    list.add(getParameter(0, parameters));
                    result = list;
                }
                break;
            }
        }

        return result;
    }

}
