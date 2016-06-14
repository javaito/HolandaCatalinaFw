package org.hcjf.io.net.http.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by javaito on 2/6/2016.
 */
public class RestPackage {

    private final Map<String, String> parameters;

    public RestPackage() {
        this.parameters = new HashMap<>();
    }

    public void addParameter(String parameterName, String parameterVaue) {
        parameters.put(parameterName, parameterVaue);
    }

    public String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

}
