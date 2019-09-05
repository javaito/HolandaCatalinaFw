package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.model.QueryReturnField;
import org.hcjf.utils.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DistinctQueryAggregateFunction extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "distinct";

    public DistinctQueryAggregateFunction() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = new ArrayList();
        Collection<QueryReturnField> fields = new ArrayList<>();
        for(Object param : parameters) {
            fields.add((QueryReturnField) param);
        }

        Set<String> hashSet = new HashSet<>();
        String hash;
        for(Object row : resultSet) {
            hash = Strings.EMPTY_STRING;
            for(QueryReturnField field : fields) {
                hash += Integer.toString(field.resolve(row).hashCode());
            }
            if(!hashSet.contains(hash)) {
                hashSet.add(hash);
                result.add(row);
            }
        }

        return result;
    }

}
