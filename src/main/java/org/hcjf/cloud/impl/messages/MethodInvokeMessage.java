package org.hcjf.cloud.impl.messages;

import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public class MethodInvokeMessage extends Message {

    private String methodName;
    private List<Class> parameterTypes;
    private List<Object> parameters;

    public MethodInvokeMessage() {
    }

    public MethodInvokeMessage(UUID id) {
        super(id);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Class> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<Class> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
}
