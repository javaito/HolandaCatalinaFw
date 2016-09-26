package org.hcjf.utils;

import org.hcjf.names.Naming;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of utilities to optimize the introspection native methods.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Introspection {

    private static final Pattern GETTER_METHODS_PATTERN = Pattern.compile("^(get|is)([1,A-Z]|[1,0-9])(.*)");
    private static final Pattern SETTER_METHODS_PATTERN = Pattern.compile("^(set)([1,A-Z]|[1,0-9])(.*)");

    private static final int SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP = 2;
    private static final int SETTER_GETTER_FIELD_NAME_GROUP = 3;

    private static final Map<String, Map<String, ? extends Invoker>> invokerCache = new HashMap<>();

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
     * Return a map with all the methods founded in the class applying the filter, indexed
     * by the filter definition.
     * @param clazz Class to be inspected.
     * @param filter Filter to apply.
     * @param <I>
     * @return Return the founded invokers.
     */
    public static <I extends Invoker> Map<String, I> getInvokers(Class clazz, InvokerFilter<I> filter) {
        Map<String, I> result = new HashMap<>();
        String invokerKey = getInvokerKey(clazz, filter);

        if(!clazz.equals(Object.class)) {
            synchronized (invokerCache) {
                if(!invokerCache.containsKey(invokerKey)) {
                    invokerCache.put(invokerKey, result);
                    for(Method method : clazz.getDeclaredMethods()) {
                        InvokerEntry<I> entry = filter.filter(method);
                        if(entry != null) {
                            result.put(entry.getKey(), entry.getInvoker());
                        }
                    }
                    if(!clazz.getSuperclass().equals(Objects.class)) {
                        result.putAll(getInvokers(clazz.getSuperclass(), filter));
                    }
                } else {
                    result = (Map<String, I>) invokerCache.get(invokerKey);
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Return the key in order to index the filter result.
     * @param clazz Class to be filtered.
     * @param filter Filter
     * @return Filter key.
     */
    private static String getInvokerKey(Class clazz, InvokerFilter filter) {
        return clazz.getName() + filter.getClass().getName();
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
     * @param namingImpl Name of the naming implementation.
     * @return All the accessors founded indexed by the possible field name.
     */
    public static Map<String, Getter> getGetters(Class clazz, String namingImpl) {
        Map<String, Getter> getters = getGetters(clazz);
        Map<String, Getter> result = new HashMap<>();

        for(String name : getters.keySet()) {
            result.put(Naming.normalize(namingImpl, name), getters.get(name));
        }

        return Collections.unmodifiableMap(result);
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
        Map<String, Getter> result = getInvokers(clazz, method -> {
            InvokerEntry<Getter> result1 = null;
            Matcher matcher;
            String fieldName;
            if(Modifier.isPublic(method.getModifiers())) {
                matcher = GETTER_METHODS_PATTERN.matcher(method.getName());
                if(matcher.matches() && !method.getReturnType().equals(Void.TYPE) &&
                        method.getParameterTypes().length == 0) {
                    fieldName = matcher.group(SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP).toLowerCase() +
                            matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                    result1 = new InvokerEntry<>(fieldName, new Getter(clazz, fieldName, method));
                }
            }
            return result1;
        });
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
    public static Map<String, Setter> getSetters(Class clazz, String namingImpl) {
        Map<String, Setter> setters = getSetters(clazz);
        Map<String, Setter> result = new HashMap<>();

        for(String name : setters.keySet()) {
            result.put(Naming.normalize(namingImpl, name), setters.get(name));
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
        Map<String, Setter> result = getInvokers(clazz, method -> {
            InvokerEntry<Setter> result1 = null;
            Matcher matcher;
            String fieldName;
            if(Modifier.isPublic(method.getModifiers())) {
                matcher = SETTER_METHODS_PATTERN.matcher(method.getName());
                if(matcher.matches() && method.getReturnType().equals(Void.TYPE) &&
                        method.getParameterTypes().length == 1) {
                    fieldName = matcher.group(SETTER_GETTER_FIRST_CHAR_FIELD_NAME_GROUP).toLowerCase() +
                            matcher.group(SETTER_GETTER_FIELD_NAME_GROUP);
                    result1 = new InvokerEntry<>(fieldName, new Setter(clazz, fieldName, method));
                }
            }
            return result1;
        });
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
                Class annotationClass = null;
                for(Class interfaceClass : annotation.getClass().getInterfaces()) {
                    if(Annotation.class.isAssignableFrom(interfaceClass)) {
                        annotationClass = interfaceClass;
                    }
                }
                annotationsMap.put(annotationClass, annotation);
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

        private final Class returnType;

        protected Getter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
            returnType = method.getReturnType();
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

        /**
         *
         * @return
         */
        public Class getReturnType() {
            return returnType;
        }
    }

    /**
     * Sub class of the accessor that represents only the setter accessors.
     */
    public static class Setter extends Accessor {

        private final Class parameterType;

        protected Setter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
            this.parameterType = method.getParameterTypes()[0];
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

        /**
         *
         * @return
         */
        public Class getParameterType() {
            return parameterType;
        }
    }

    /**
     * This interface must be implemented to found some kind of methods implemented
     * in the any class.
     * @param <I>
     */
    public interface InvokerFilter<I extends Invoker> {

        /**
         * This method will be called for each method of the filtered class.
         * @param method Declared method.
         * @return Return the entry or null if the method does not comply with the rule
         */
        public InvokerEntry<I> filter(Method method);

    }

    /**
     * This class represents the object returned by the invoker filter.
     * @param <I>
     */
    private static class InvokerEntry<I extends Invoker> {

        private final String key;
        private final I invoker;

        public InvokerEntry(String key, I invoker) {
            this.key = key;
            this.invoker = invoker;
        }

        /**
         * Return the key of the entry.
         * @return Key of the entry.
         */
        public String getKey() {
            return key;
        }

        /**
         * Return the value of the entry.
         * @return Value of the entry.
         */
        public I getInvoker() {
            return invoker;
        }
    }
}
