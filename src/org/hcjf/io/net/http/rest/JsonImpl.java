package org.hcjf.io.net.http.rest;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

/**
 * Created by javaito on 2/6/2016.
 */
public abstract class JsonImpl<O extends RestPackage> extends EndPointImpl<O> {

    public static final String JSON_FORMAT = "json";

    public JsonImpl(String version) {
        super(version, JSON_FORMAT);
    }

    @Override
    protected O encode(byte[] body) {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Class genericClass = (Class) parameterizedType.getActualTypeArguments()[0];
        Gson gson = new Gson();
        return (O) gson.fromJson(new String(body), genericClass);
    }

    @Override
    protected byte[] decode(O body) {
        Gson gson = new Gson();
        return gson.toJson(body).getBytes();
    }

    @Override
    protected String getContentType() {
        return "text/json";
    }

}
