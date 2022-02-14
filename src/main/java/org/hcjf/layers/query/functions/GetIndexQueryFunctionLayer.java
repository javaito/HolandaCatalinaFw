package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.Enlarged;
import java.util.Collection;

public class GetIndexQueryFunctionLayer extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "getIndex";

    public GetIndexQueryFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        int countValue = 0;
        for (Object row : resultSet) {
            countValue++;
            ((Enlarged) row).put(alias, countValue);
        }

        return result;
    }
}
