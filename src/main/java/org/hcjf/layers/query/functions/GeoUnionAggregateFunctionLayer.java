package org.hcjf.layers.query.functions;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.hcjf.layers.query.Enlarged;
import org.hcjf.layers.query.model.QueryReturnField;
import org.hcjf.utils.GeoUtils;
import org.hcjf.utils.JsonUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GeoUnionAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "geoAggregateUnion";

    public GeoUnionAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection collection;
        for(Object row : (Collection<Enlarged>)resultSet) {
            Object parameterValue = resolveValue(row, parameters[0]);
            if(parameterValue instanceof Collection) {
                collection = (Collection) parameterValue;
            } else if(parameterValue.getClass().isArray()) {
                collection = Arrays.asList(parameterValue);
            } else {
                collection = List.of(parameterValue);
            }

            OGCGeometry geometry = null;
            for(Object object : collection) {
                if(object == null) {
                    continue;
                }

                Object value = object;
                if(value instanceof QueryReturnField) {
                    value = ((QueryReturnField)value).resolve(row);
                }
                if(geometry == null) {
                    geometry = GeoUtils.createGeometry(value);
                } else {
                    geometry = geometry.union(GeoUtils.createGeometry(value));
                }
            }

            Object result = null;
            if(geometry != null) {
                result = JsonUtils.createObject(geometry.asGeoJson());
            }
            ((Enlarged)row).put(alias, result);
        }

        return resultSet;
    }
}
