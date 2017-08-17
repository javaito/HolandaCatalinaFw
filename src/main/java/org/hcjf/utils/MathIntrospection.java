package org.hcjf.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * This class contains the default filters and invokers to
 * create the static introspection of {@link java.lang.Math} class
 * @author javaito.
 */
public class MathIntrospection {

    private static final MathInvokerFilter filter = new MathInvokerFilter();

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
                result = new Introspection.InvokerEntry<>(method.getName(),
                        new MathInvoker(method.getDeclaringClass(), method));
            }

            return result;
        }

    }

}
