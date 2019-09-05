package org.hcjf.layers.query.model;

public interface QueryReturnParameter extends QueryComponent {

    /**
     * Return the field alias, can be null.
     * @return Field alias.
     */
    String getAlias();

}
