package org.hcjf.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Introspection {

    private static final Pattern GETTER_METHODS_PATTERN = Pattern.compile("^(get|is)([1,A-Z]|[1,0-9])(.*)");
    private static final Pattern SETTER_METHODS_PATTERN = Pattern.compile("^(set)([1,A-Z]|[1,0-9])(.*)");

    private static final int SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP = 2;
    private static final int SETTER_GETTER_FIELD_NAME_GROUP = 3;

    private static final Map<Class, Map<String, Getter>> gettersCache = new HashMap<>();
    private static final Map<Class, Map<String, Setter>> settersCache = new HashMap<>();

    /**
     * Returns the subclass of the class specified as a parameter whose simple name
     * is also indicated as a parameter
     * @param className Capitalized or uncapitalized simple name of the founded subclass.
     * @param superClass Super class of this king of implementations.
     * @return Sub class funded.
     */
    public static Class classForName(String className, Class superClass) {
        String name = superClass.getName();
        String packageName = name.substring(0, name.lastIndexOf(".") + 1);
        String subClassName = Strings.capitalize(className);
        try {
            return Class.forName(packageName + subClassName);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Sub class of " + superClass.getName() + " called " + className + " not found", ex);
        }
    }

    /**
     * Return a map with all the getters accessor instances indexed by the expected name of the
     * field that represents each accessor. The fields represented by the name can exists or no.
     * The accessor instances contains all the information about accessor method and the annotation
     * bounded to the method.
     * The found methods comply with the following regular expression and conditions:
     * <li>^(get|is)([1,A-Z]|[1,0-9])(.*)</li>
     * <li>must return something distinct to void type</li>
     * <li>without parameters</li>
     * <li>must be a public method</li>
     * @param clazz Class definition to found the getters method.
     * @return All the accessors founded indexed by the possible field name.
     */
    public static Map<String, Getter> getGetters(Class clazz) {
        Map<String, Getter> result = new HashMap<>();

        if(!clazz.equals(Object.class)) {
            synchronized (gettersCache) {
                if(!gettersCache.containsKey(clazz)) {
                    gettersCache.put(clazz, result);
                    Matcher matcher;
                    String fieldName;
                    for(Method method : clazz.getDeclaredMethods()) {
                        if(Modifier.isPublic(method.getModifiers())) {
                            matcher = GETTER_METHODS_PATTERN.matcher(method.getName());
                            if(matcher.matches() && !method.getReturnType().equals(Void.TYPE) &&
                                    method.getParameterTypes().length == 0) {
                                fieldName = matcher.group(SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP).toLowerCase() +
                                        matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                                result.put(fieldName, new Getter(clazz, fieldName, method));
                            }
                        }
                    }
                } else {
                    result = gettersCache.get(clazz);
                }
            }

            if(!clazz.getSuperclass().equals(Objects.class)) {
                result.putAll(getGetters(clazz.getSuperclass()));
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Return a map with all the setters accessor instances indexed by the expected name of the
     * field that represents each accessor. The fields represented by the name can exists or no.
     * The accessor instances contains all the information about accessor method and the annotation
     * bounded to the method.
     * The found methods comply with the following regular expression and conditions:
     * <li>^(set)([1,A-Z]|[1,0-9])(.*)</li>
     * <li>must return void type.</li>
     * <li>with only one parameter</li>
     * <li>must be a public method</li>
     * @param clazz Class definition to found the setter method.
     * @return All the accessors founded indexed by the possible field name.
     */
    public static Map<String, Setter> getSetters(Class clazz) {
        Map<String, Setter> result = new HashMap<>();

        if(!clazz.equals(Object.class)) {
            synchronized (settersCache) {
                if (!settersCache.containsKey(clazz)) {
                    settersCache.put(clazz, result);
                    Matcher matcher;
                    String fieldName;
                    for(Method method : clazz.getDeclaredMethods()) {
                        if(Modifier.isPublic(method.getModifiers())) {
                            matcher = SETTER_METHODS_PATTERN.matcher(method.getName());
                            if(matcher.matches() && method.getReturnType().equals(Void.TYPE) &&
                                    method.getParameterTypes().length == 1) {
                                fieldName = matcher.group(SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP).toLowerCase() +
                                        matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                                result.put(fieldName, new Setter(clazz, fieldName, method));
                            }
                        }
                    }
                } else {
                    result = settersCache.get(clazz);
                }
            }

            if(!clazz.getSuperclass().equals(Objects.class)) {
                result.putAll(getSetters(clazz.getSuperclass()));
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public static abstract class Invoker {

        private final Class implementationClass;
        private final Method method;
        private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

        public Invoker(Class implementationClass, Method method) {
            this.implementationClass = implementationClass;
            this.method = method;
            this.annotationsMap = new HashMap<>();
            for(Annotation annotation : method.getAnnotations()) {
                annotationsMap.put(annotation.getClass(), annotation);
            }
        }

        /**
         * Return the the name of the class when the method is implemented.
         * @return Implementation class name.
         */
        public final Class getImplementationClass() {
            return implementationClass;
        }

        /**
         * Return the name of the accessor method.
         * @return Name of the accessor method.
         */
        public final Method getMethod() {
            return method;
        }

        /**
         * Verify if exists an instance of the annotation class associated to the accessor method.
         * @param annotationClass Annotation class.
         * @return True if the instance exists and false in the other ways.
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return annotationsMap.containsKey(annotationClass);
        }

        /**
         * Return the instance of the annotation class associated to the accessor method, or null if
         * the annotation doesn't exist.
         * @param annotationClass Annotation class.
         * @return Annotation instance or null.
         */
        public final <A extends Annotation> A getAnnotation(Class<? extends A> annotationClass) {
            A result = null;
            if(annotationsMap.containsKey(annotationClass)) {
                result = (A) annotationsMap.get(annotationClass);
            }
            return result;
        }

        /**
         * Return an unmodifiable map with all the annotation instances associated to the method indexed
         * by the class of the each annotation instance.
         * @return Unmodifiable map.
         */
        public final Map<Class<? extends Annotation>, Annotation> getAnnotationsMap() {
            return Collections.unmodifiableMap(annotationsMap);
        }

        /**
         * Wrapper method to get the storage method.
         * @param instance Instance to get the mehtod.
         * @param params Method parameters.
         * @return Invokation result.
         */
        public Object invoke(Object instance, Object... params) throws InvocationTargetException, IllegalAccessException {
            return getMethod().invoke(instance, params);
        }
    }

    /**
     * This class groups all the information about getter/setter accessor methods.
     */
    public static abstract class Accessor extends Invoker {

        private final String resourceName;

        protected Accessor(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, method);
            this.resourceName = resourceName;
        }

        /**
         * Return the name of the resource, this name is a representation based on the
         * accessor method name. if the accessor name is 'getA' then the resource name is 'a'.
         * @return Resource name.
         */
        public final String getResourceName() {
            return resourceName;
        }

    }

    /**
     * Sub class of the accessor that represents only the getter accessors.
     */
    public static class Getter extends Accessor {

        protected Getter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
        }

        /**
         * Reflection invoked by the underlying method to obtain the resource value.
         * @param instance Instance to do reflection.
         * @param <O> Expected result type for the client.
         * @return Resource value.
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        public <O extends Object> O get(Object instance) throws InvocationTargetException, IllegalAccessException {
            return (O) invoke(instance);
        }
    }

    /**
     * Sub class of the accessor that represents only the setter accessors.
     */
    public static class Setter extends Accessor {

        protected Setter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
        }

        /**
         * Reflection invoked by the underlying method to set the resource value.
         * @param instance Instance to do reflection.
         * @param value Parameter value.
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        public void set(Object instance, Object value) throws InvocationTargetException, IllegalAccessException {
            invoke(instance, value);
        }

    }
}
