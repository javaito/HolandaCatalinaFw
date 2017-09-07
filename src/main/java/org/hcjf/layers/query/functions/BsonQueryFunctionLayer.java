package org.hcjf.layers.query.functions;

import org.hcjf.bson.BsonArray;
import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonElement;
import org.hcjf.properties.SystemProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains a set of function to work with bson objects.
 * @author javaito
 */
public class BsonQueryFunctionLayer extends BaseQueryFunctionLayer {

    private static final String BSON_PARSE = "bsonParse";

    public BsonQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.BSON_FUNCTION_NAME));

        addFunctionName(BSON_PARSE);
    }

    /**
     * Evaluates the namo of the function and delegates to resolve the specific function.
     * @param functionName Function name.
     * @param parameters Function's parameters.
     * @return Returns the value that is the result of the specific function.
     */
    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result = null;
        switch(functionName) {
            case BSON_PARSE: result = createMap(BsonDecoder.decode((byte[]) checkSize(1, parameters)[0])); break;
        }
        return result;
    }

    /**
     * This method creates and returns a map with all the fields of the bson document.
     * @param document Bson document.
     * @return Map with all the fields and values.
     */
    private Map<String,Object> createMap(BsonDocument document) {
        Map<String,Object> result = new HashMap<>();
        Map<String, BsonElement> bsonMap = document.getValue();
        BsonElement element;
        for(String name : bsonMap.keySet()) {
            element = document.get(name);
            if(element instanceof BsonDocument) {
                result.put(name, createMap((BsonDocument) element));
            } else if(element instanceof BsonArray) {
                result.put(name, createList((BsonArray)element));
            } else {
                result.put(name, element.getValue());
            }
        }
        return result;
    }

    /**
     * This mehtod creates and returns a list with all the values of the bson array.
     * @param array Bson array.
     * @return List with all the values.
     */
    private List<Object> createList(BsonArray array) {
        List<Object> result = new ArrayList<>();
        BsonElement element;
        for (int i = 0; i < array.getLength(); i++) {
            element = array.get(i);
            if(element instanceof BsonDocument) {
                result.add(createMap((BsonDocument) element));
            } else if(element instanceof BsonArray) {
                result.add(createList((BsonArray)element));
            } else {
                result.add(element.getValue());
            }
        }
        return result;
    }
}
