package org.hcjf.layers.query;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.JsonUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class JsonQuery {

    /**
     * Evaluates the query instance using the information contained into the json.
     * @param queryable Queryable instance.
     * @param json Json as string.
     * @return Return the result set.
     */
    public static Collection<Map<String,Object>> evaluate(Queryable queryable, String json) {
        Object jsonObject = JsonUtils.createObject(json);
        Collection<Map<String,Object>> source = new ArrayList<>();
        if(jsonObject instanceof Collection) {
            source.addAll((Collection)jsonObject);
        } else if(jsonObject instanceof Map){
            source.add((Map<String, Object>) jsonObject);
        } else {
            throw new HCJFRuntimeException("Illegal data type in order to create data source");
        }
        return queryable.evaluate(source);
    }

    /**
     * Evaluates the query instance using the information contained into the json file.
     * @param queryable Queryable instance.
     * @param path Json file.
     * @return Return the result set.
     */
    public static Collection<Map<String,Object>> evaluate(Queryable queryable, Path path) {
        try {
            return evaluate(queryable, new String(Files.readAllBytes(path)));
        } catch (IOException ex) {
            throw new HCJFRuntimeException("Fail to read path in order to crear the source", ex);
        }
    }

    /**
     * Evaluates the query instance using the information contained into the json file.
     * @param queryable Queryable instance.
     * @param jsonFile Json file.
     * @return Return the result set.
     */
    public static Collection<Map<String,Object>> evaluate(Queryable queryable, ByteBuffer jsonFile) {
        byte[] array = new byte[jsonFile.limit() - jsonFile.position()];
        jsonFile.get(array);
        return evaluate(queryable, new String(array));
    }

}
