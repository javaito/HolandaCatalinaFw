package org.hcjf.layers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to identify if some implementation is the default
 * implementation to resolve a set of interfaces.
 * @author javaito
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultLayer {

    /**
     * Contains all the interfaces that the annotated class is a default implementation.
     * @return Set of interfaces.
     */
    Class<? extends LayerInterface>[] value();

}
