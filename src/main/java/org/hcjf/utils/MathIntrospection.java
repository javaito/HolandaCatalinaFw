package org.hcjf.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the default filters and invokers to
 * create the static introspection of {@link java.lang.Math} class
 * @author javaito.
 */
public class MathIntrospection {

    private static final MathInvokerFilter filter = new MathInvokerFilter();
    private static final Map<String, Integer> typeWeight;

    static {
        typeWeight = new HashMap<>();
        typeWeight.put("byte", 1);
        typeWeight.put("short", 2);
        typeWeight.put("int", 3);
        typeWeight.put("integer", 3);
        typeWeight.put("long", 4);
        typeWeight.put("float", 5);
        typeWeight.put("double", 6);
    }

    /**
     * This method invokes the specific method of the {@link java.lang.Math} class
     * using the internal introspection cache.
     * @param functionName Name of the math function.
     * @param parameter Function's parameters.
     * @param <R> Expected return data type.
     * @return Math function result.
     */
    public static <R extends Object> R  invoke(String functionName, Object... parameter) {
        try {
            return (R) Introspection.getInvokers(Math.class, filter).get(functionName).invoke(null, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns all the methods cached fot the {@link java.lang.Math} class
     * @return Methods set.
     */
    public static Set<String> getMethodsSet() {
        return Introspection.getInvokers(Math.class, filter).keySet();
    }

    /**
     * This invoker implementation represents each static method in the {@link java.lang.Math} class
     */
    private static class MathInvoker extends Introspection.Invoker {

        public MathInvoker(Class implementationClass, Method method) {
            super(implementationClass, method);
        }

    }

    /**
     * Invoker filter to create the introspection cache for the {@link java.lang.Math} class
     */
    private static class MathInvokerFilter implements Introspection.InvokerFilter<MathInvoker> {

        private final Map<String, Integer> weightByMethodName;

        public MathInvokerFilter() {
            weightByMethodName = new HashMap<>();
        }

        /**
         * If the method is implemented into the {@link java.lang.Math} class and this
         * method is static then the method is accepted.
         * @param method Declared method.
         * @return Accepted method entry or null.
         */
        @Override
        public Introspection.InvokerEntry<MathInvoker> filter(Method method) {
            Introspection.InvokerEntry<MathInvoker> result = null;

            if(Modifier.isPublic(method.getModifiers()) &&
                    Modifier.isStatic(method.getModifiers()) &&
                    method.getDeclaringClass().equals(Math.class)) {

                Boolean create = false;
                Integer weight = calculateMethodWeight(method);
                if(weightByMethodName.containsKey(method.getName())) {
                    if(weight > weightByMethodName.get(method.getName())) {
                        create = true;
                    }
                } else {
                    create = true;
                }

                if(create) {
                    result = new Introspection.InvokerEntry<>(method.getName(),
                            new MathInvoker(method.getDeclaringClass(), method));
                    weightByMethodName.put(method.getName(), weight);
                }
            }

            return result;
        }

        private Integer calculateMethodWeight(Method method) {
            Integer result = 0;

            Integer weight;
            for(Class dataType : method.getParameterTypes()) {
                weight = typeWeight.get(dataType.getSimpleName().toLowerCase());
                if(weight != null) {
                    result += weight;
                }
            }

            return result;
        }

    }

}
