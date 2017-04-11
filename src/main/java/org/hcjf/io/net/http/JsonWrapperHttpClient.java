package org.hcjf.io.net.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.JsonIntrospection;

import java.net.URL;
import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class JsonWrapperHttpClient extends WrapperHttpClient {

    public JsonWrapperHttpClient(URL url) {
        super(url);
    }

    /**
     *
     * @param resourceClass
     * @param <O>
     * @return
     */
    @Override
    public final <O extends Object> O getResource(Class<? extends O> resourceClass) {
        try {
            return completeResource(resourceClass.newInstance());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create instance of the resource class", ex);
        }
    }

    /**
     *
     * @param resource
     * @param <O>
     * @return
     */
    @Override
    public final <O extends Object> O completeResource(O resource) {
        HttpResponse response = request();

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new String(response.getBody()));

        O result = resource;
        Map<String, Introspection.Setter> setters = Introspection.getSetters(resource.getClass());
        Introspection.Setter setter;
        Object value;
        for(String src : getMapping().keySet()) {
            String dest = getMapping().get(src);
            setter = setters.get(dest);
            if(setter == null) {
                throw new IllegalArgumentException("");
            }

            value = JsonIntrospection.getValue(jsonElement, src, setter.getParameterType());
            try {
                setter.invoke(result, value);
            } catch (Exception ex) {
                throw new RuntimeException("Setter invocation fail", ex);
            }
        }

        return result;
    }
}
