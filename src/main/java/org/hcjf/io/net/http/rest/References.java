package org.hcjf.io.net.http.rest;

import java.util.Collection;

/**
 * This interface provides all the methods to implements
 * crud references.
 * @author javaito
 */
public interface References {

    String REFERENCES_FIELD_NAME = "__references__";

    /**
     * Returns the instance of the reference indexed by specific name.
     * @param referenceName Reference name.
     * @param <O> Expected reference class.
     * @return Reference instance.
     */
    <O extends Object> O getReference(String referenceName);

    /**
     * Returns a collection of reference instances indexed by the specific name.
     * @param referenceName Reference name.
     * @param <O> Expected reference class.
     * @return Reference instances.
     */
    <O> Collection<O> getReferenceCollection(String referenceName);

}
