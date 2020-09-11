package org.hcjf.layers.query.compilers;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;

public interface QueryCompiler extends LayerInterface {

    /**
     * Create a query instance from a expression.
     * @param queryExpression Expression that represents a query.
     * @return Query instance.
     */
    Query compile(String queryExpression);

}
