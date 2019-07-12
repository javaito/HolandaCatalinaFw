package org.hcjf.layers.query.functions;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.hcjf.layers.query.Enlarged;
import org.hcjf.utils.GeoUtils;
import org.hcjf.utils.JsonUtils;

import java.lang.reflect.Array;
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
        if(parameters[0] instanceof Collection) {
            collection = (Collection) parameters[0];
        } else if(parameters[0].getClass().isArray()) {
            collection = Arrays.asList(parameters[0]);
        } else {
            collection = List.of(parameters[0]);
        }

        OGCGeometry geometry = null;
        for(Object object : collection) {
            if(object == null) {
                continue;
            }

            if(geometry == null) {
                geometry = GeoUtils.createGeometry(object);
            } else {
                geometry = geometry.union(GeoUtils.createGeometry(object));
            }
        }

        Object result = null;
        if(geometry != null) {
            result = JsonUtils.createObject(geometry.asGeoJson());
        }

        for(Enlarged enlarged : (Collection<Enlarged>)resultSet) {
            enlarged.put(alias, result);
        }

        return resultSet;
    }
}
