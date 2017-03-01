package org.hcjf.utils;

import org.hcjf.names.Naming;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
     * Creates a mpa using all the getters method of the class.
     * @param instance Instance to transform.
     * @return Map
     */
    public static Map<String, Object> toMap(Object instance) {
        return toMap(instance, (O)->O);
    }

    /**
     * Creates a mpa (String key and String value) using all the getters method of the class.
     * @param instance Instance to transform.
     * @return Map
     */
    public static Map<String, String> toStringsMap(Object instance) {
        return toMap(instance, (O)->O.toString());
    }

    /**
     * Creates a mpa using all the getters method of the class.
     * @param instance Instance to transform.
     * @param consumer Instance of the consumer to transform the values of the
     *                 instance to the expected values into the map.
     * @return Map
     */
    public static <O extends Object> Map<String, O> toMap(Object instance, Consumer consumer) {
        Map<String, O> result = new HashMap<>();
        if(instance instanceof Map) {
            result = (Map<String, O>) instance;
        } else {
            Map<String, Getter> getters = getGetters(instance.getClass());
            Object value;
            for (String name : getters.keySet()) {
                try {
                    value = getters.get(name).get(instance);
                    if (value != null) {
                        result.put(name, (O) consumer.consume(value));
                    }
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    /**
     * Create an instance of the class from a map.
     * @param map Map with values.
     * @param clazz Instance class.
     * @param <O> Expected type.
     * @return Instance.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <O extends Object> O toInstance(Map<String, Object> map, Class<O> clazz) throws IllegalAccessException, InstantiationException {
        O result = clazz.newInstance();
        Map<String, Setter> setters = getSetters(clazz);
        for(String name : setters.keySet()) {
            if(map.containsKey(name)) {
                try {
                    setters.get(name).set(result, map.get(name));
                } catch (Exception ex){}
            }
        }
        return result;
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
                            for(String alias : entry.getAliases()) {
                                result.put(alias, entry.getInvoker());
                            }
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
                    result1 = new InvokerEntry<>(fieldName, new Getter(method.getDeclaringClass(), fieldName, method));
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
                    result1 = new InvokerEntry<>(fieldName, new Setter(method.getDeclaringClass(), fieldName, method));
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
        private final Class returnKeyType;
        private final Class returnCollectionType;

        protected Getter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
            returnType = method.getReturnType();

            if (method.getGenericReturnType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                if(Collection.class.isAssignableFrom(returnType)) {
                    returnKeyType = null;
                    returnCollectionType = (Class) parameterizedType.getActualTypeArguments()[0];
                } else if(Map.class.isAssignableFrom(returnType)) {
                    returnKeyType = (Class) parameterizedType.getActualTypeArguments()[0];
                    returnCollectionType = (Class) parameterizedType.getActualTypeArguments()[1];
                } else {
                    returnKeyType = null;
                    returnCollectionType = null;
                }
            } else {
                returnKeyType = null;
                returnCollectionType = null;
            }
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
         * Return the result type of the getter method.
         * @return Result type.
         */
        public final Class getReturnType() {
            return returnType;
        }

        /**
         * If the result type is assignable to map class then this method return
         * the key type of the result type.
         * @return Key type of the map.
         */
        public final Class getReturnKeyType() {
            return returnKeyType;
        }

        /**
         * If the result type if assignable to collection or map this this mehtod return
         * the collection types of the result type.
         * @return Collection type of the result type.
         */
        public final Class getReturnCollectionType() {
            return returnCollectionType;
        }
    }

    /**
     * Sub class of the accessor that represents only the setter accessors.
     */
    public static class Setter extends Accessor {

        private final Class parameterType;
        private final Class parameterKeyType;
        private final Class parameterCollectionType;

        protected Setter(Class implementationClass, String resourceName, Method method) {
            super(implementationClass, resourceName, method);
            this.parameterType = method.getParameterTypes()[0];

            if(method.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericParameterTypes()[0];
                if(Collection.class.isAssignableFrom(parameterType)) {
                    parameterKeyType = null;
                    parameterCollectionType = (Class) parameterizedType.getActualTypeArguments()[0];
                } else if(Map.class.isAssignableFrom(parameterType)) {
                    parameterKeyType = (Class) parameterizedType.getActualTypeArguments()[0];
                    parameterCollectionType = (Class) parameterizedType.getActualTypeArguments()[1];
                } else {
                    parameterKeyType = null;
                    parameterCollectionType = null;
                }
            } else {
                parameterKeyType = null;
                parameterCollectionType = null;
            }
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
         * Return the parameter type of the setter method.
         * @return Parameter type.
         */
        public final Class getParameterType() {
            return parameterType;
        }

        /**
         * If the parameter type is assignable to map class then this method
         * return the key type of the parameter type.
         * @return Key type.
         */
        public final Class getParameterKeyType() {
            return parameterKeyType;
        }

        /**
         * If the parameter type is assignable to map class or collection class then this
         * method return the collection type of the parameter type.
         * @return Collection type.
         */
        public final Class getParameterCollectionType() {
            return parameterCollectionType;
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
    public static class InvokerEntry<I extends Invoker> {

        private final String key;
        private final I invoker;
        private final String[] aliases;

        public InvokerEntry(String key, I invoker, String... aliases) {
            this.key = key;
            this.invoker = invoker;
            this.aliases = aliases;
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

        /**
         * Return the aliases array.
         * @return Aliases array.
         */
        public String[] getAliases() {
            return aliases;
        }
    }

    /**
     * This interface represents a possible implementation of the way to consume
     * a getter method of the some kind of object.
     */
    public interface Consumer {

        /**
         * The implementation of this method consume the value a return
         * the same value or other instance based on this value.
         * @return Return a new representation of the value or the same object
         * depends the consumer implementation.
         */
        public Object consume(Object value);

    }

}
