package org.hcjf.layers.query.serializer;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;

public interface QuerySerializer extends LayerInterface {

    /**
     * This method creates query expression from a query instance.
     * @param query Query instance.
     * @return Query expression.
     */
    String serialize(Query query);

}
