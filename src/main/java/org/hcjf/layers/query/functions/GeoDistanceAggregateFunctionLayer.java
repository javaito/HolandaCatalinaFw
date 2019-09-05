package org.hcjf.layers.query.functions;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Enlarged;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.model.QueryReturnField;
import org.hcjf.utils.GeoUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class GeoDistanceAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "geoAggregateDistance";

    public GeoDistanceAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;

        if (parameters.length == 0) {
            throw new HCJFRuntimeException("The geoAggregateDistance function need at least one parameter");
        }

        OGCGeometry previousGeometry = null;
        OGCGeometry currentGeometry;
        double totalDistance = 0.0;
        double distance = 0.0;
        boolean accumulate = parameters.length >= 2 && (boolean) parameters[1];
        boolean group = parameters.length >= 3 && (boolean) parameters[2];
        for(Object row : result) {
            currentGeometry = GeoUtils.createGeometry(resolveValue(row, parameters[0]));
            if (previousGeometry != null) {
                distance = previousGeometry.centroid().distance(currentGeometry.centroid()) * 100;
                totalDistance += distance;
            }
            previousGeometry = currentGeometry;
            if(!group) {
                if (accumulate) {
                    ((Enlarged) row).put(alias, totalDistance);
                } else {
                    ((Enlarged) row).put(alias, distance);
                }
            }
        }

        if(group) {
            Collection<JoinableMap> newResultSet = new ArrayList<>();
            JoinableMap totalDistanceRow = new JoinableMap(new HashMap<>(), alias);
            totalDistanceRow.put(alias, totalDistance);
            newResultSet.add(totalDistanceRow);
            result = newResultSet;
        }

        return result;
    }

}
