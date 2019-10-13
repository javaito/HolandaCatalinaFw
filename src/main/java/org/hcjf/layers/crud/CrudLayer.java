package org.hcjf.layers.crud;

import org.hcjf.layers.AdaptableLayer;
import org.hcjf.layers.Layer;
import org.hcjf.utils.Introspection;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author javaito
 */
public abstract class CrudLayer<O extends Object> extends Layer implements AdaptableLayer {

    private Class<O> resourceType;

    public CrudLayer(String implName) {
        super(implName);
    }

    public CrudLayer() {
    }

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    public synchronized final Class<O> getResourceType() {
        if(resourceType == null) {
            Class currentClass = getClass();
            Type genericSuperClass = currentClass.getGenericSuperclass();
            while (currentClass != Object.class &&
                    !(genericSuperClass instanceof ParameterizedType)) {
                currentClass = currentClass.getSuperclass();
                genericSuperClass = currentClass.getGenericSuperclass();
            }

            if (genericSuperClass instanceof ParameterizedType) {
                Type actualType = ((ParameterizedType) genericSuperClass).
                        getActualTypeArguments()[0];
                if (actualType instanceof ParameterizedType) {
                    resourceType = (Class<O>) ((ParameterizedType) actualType).getRawType();
                } else {
                    resourceType = (Class<O>) actualType;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return resourceType;
    }

    @Override
    public Object[] adaptArguments(Method method, Object[] args) {
        Object[] result = args;
        if(!Map.class.isAssignableFrom(getResourceType())) {
            if (method.getDeclaringClass().equals(CreateLayerInterface.class)) {
                result[0] = adaptObject((Map<String, Object>) result[0]);
            } else if (method.getDeclaringClass().equals(UpdateLayerInterface.class)) {
                if(args.length == 1) {
                    result[0] = adaptObject((Map<String, Object>) result[0]);
                } else {
                    result[1] = adaptObject((Map<String, Object>) result[1]);
                }
            }
        }
        return result;
    }

    private O adaptObject(Map<String, Object> parameter) {
        O result = null;
        if(parameter != null) {
            try {
                result = Introspection.toInstance(parameter, getResourceType());
            } catch (Exception ex) {
                throw new RuntimeException("Unable to adapt the call arguments", ex);
            }
        }
        return result;
    }
}
