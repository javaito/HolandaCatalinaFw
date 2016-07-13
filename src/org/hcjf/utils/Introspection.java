package org.hcjf.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Introspection {

    private static final Pattern GETTER_METHODS_PATTERN = Pattern.compile("^(get|is)([1,A-Z]|[1,0-9])(.*)");
    private static final Pattern SETTER_METHODS_PATTERN = Pattern.compile("^(set)([1,A-Z]|[1,0-9])(.*)");

    private static final int SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP = 2;
    private static final int SETTER_GETTER_FIELD_NAME_GROUP = 3;

    /**
     *
     * @param clazz
     * @return
     */
    public static Map<String, Getter> getGetters(Class clazz) {
        Map<String, Getter> result = new HashMap<>();

        Matcher matcher;
        String fieldName;
        if(!clazz.equals(Object.class)) {
            for(Method method : clazz.getDeclaredMethods()) {
                if(Modifier.isPublic(method.getModifiers())) {
                    matcher = GETTER_METHODS_PATTERN.matcher(method.getName());
                    if(matcher.matches()) {
                        fieldName = matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                        result.put(fieldName, new Getter(clazz, fieldName, method));
                    }
                }
            }
            if(!clazz.getSuperclass().equals(Objects.class)) {
                result.putAll(getGetters(clazz.getSuperclass()));
            }
        }

        return result;
    }

    /**
     *
     * @param clazz
     * @return
     */
    public static Map<String, Setter> getSetters(Class clazz) {
        Map<String, Setter> result = new HashMap<>();

        Matcher matcher;
        String fieldName;
        if(!clazz.equals(Object.class)) {
            for(Method method : clazz.getDeclaredMethods()) {
                if(Modifier.isPublic(method.getModifiers())) {
                    matcher = SETTER_METHODS_PATTERN.matcher(method.getName());
                    if(matcher.matches()) {
                        fieldName = matcher.group(SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP).toLowerCase() +
                                matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                        result.put(fieldName, new Setter(clazz, fieldName, method));
                    }
                }
            }
            if(!clazz.getSuperclass().equals(Objects.class)) {
                result.putAll(getSetters(clazz.getSuperclass()));
            }
        }

        return result;
    }

    /**
     *
     */
    public static abstract class Accessor {

        private final Class implementationClass;
        private final String resourceName;
        private final Method method;

        protected Accessor(Class implementationClass, String resourceName, Method method) {
            this.implementationClass = implementationClass;
            this.resourceName = resourceName;
            this.method = method;
        }

        /**
         *
         * @return
         */
        public Class getImplementationClass() {
            return implementationClass;
        }

        /**
         *
         * @return
         */
        public String getResourceName() {
            return resourceName;
        }

        /**
         *
         * @return
         */
        public Method getMethod() {
            return method;
        }
    }

    /**
     *
     */
    public static class Getter extends Accessor {

        protected Getter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
        }

        /**
         *
          * @param instance
         * @param <O>
         * @return
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        public <O extends Object> O invoke(Object instance) throws InvocationTargetException, IllegalAccessException {
            return (O) getMethod().invoke(instance);
        }
    }

    /**
     *
     */
    public static class Setter extends Accessor {

        protected Setter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
        }

        /**
         *
         * @param instance
         * @param value
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        public void invoke(Object instance, Object value) throws InvocationTargetException, IllegalAccessException {
            getMethod().invoke(instance, value);
        }
    }
}
