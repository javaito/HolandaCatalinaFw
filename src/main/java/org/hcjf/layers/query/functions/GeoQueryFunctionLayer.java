package org.hcjf.layers.query.functions;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCLineString;
import com.esri.core.geometry.ogc.OGCPolygon;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.GeoUtils;
import org.hcjf.utils.JsonUtils;

public class GeoQueryFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String NAME = "geo";

    private static final class Functions {
        private static final String GEO_NEW = "geoNew";
        private static final String GEO_AS_JSON = "geoAsJson";
        private static final String GEO_AS_GEO_JSON = "geoAsGeoJson";
        private static final String GEO_AS_BINARY = "geoAsBinary";
        private static final String GEO_AS_TEXT = "geoAsText";
        private static final String GEO_BOUNDARY = "geoBoundary";
        private static final String GEO_BUFFER = "geoBuffer";
        private static final String GEO_CENTROID = "geoCentroid";
        private static final String GEO_CONTAINS = "geoContains";
        private static final String GEO_CONVERT_TO_MULTI = "geoConvertToMulti";
        private static final String GEO_CROSSES = "geoCrosses";
        private static final String GEO_DIFFERENCE = "geoDifference";
        private static final String GEO_DISJOINT = "geoDisjoint";
        private static final String GEO_DISTANCE = "geoDistance";
        private static final String GEODESIC_DISTANCE = "geodesicDistance";
        private static final String GEO_ENVELOPE = "geoEnvelope";
        private static final String GEO_EQUALS = "geoEquals";
        private static final String GEO_TYPE = "geoType";
        private static final String GEO_INTERSECTION = "geoIntersection";
        private static final String GEO_INTERSECTS = "geoIntersects";
        private static final String GEO_IS_EMPTY = "geoIsEmpty";
        private static final String GEO_IS_MEASURED = "geoIsMeasured";
        private static final String GEO_IS_SIMPLE = "geoIsSimple";
        private static final String GEO_IS_SIMPLE_RELAXED = "geoIsSimpleRelaxed";
        private static final String GEO_MAKE_SIMPLE = "geoMakeSimple";
        private static final String GEO_MAKE_SIMPLE_RELAXED = "geoMakeSimpleRelaxed";
        private static final String GEO_OVERLAPS = "geoOverlaps";
        private static final String GEO_REDUCE_FROM_MULTI = "geoReduceFromMulti";
        private static final String GEO_SRID = "geoSrid";
        private static final String GEO_SYM_DIFFERENCE = "geoSymDifference";
        private static final String GEO_TOUCHES = "geoTouches";
        private static final String GEO_UNION = "geoUnion";
        private static final String GEO_WITHIN = "geoWithin";
        private static final String GEO_PROJECT = "geoProject";
        private static final String DEGREES_TO_DECIMAL = "degreesToDecimal";
        private static final String AREA = "area";
        private static final String PERIMETER = "perimeter";
        private static final String LINE_DISTANCE = "lineDistance";
    }

    public GeoQueryFunctionLayer() {
        super(NAME);

        addFunctionName(Functions.GEO_NEW);
        addFunctionName(Functions.GEO_AS_BINARY);
        addFunctionName(Functions.GEO_AS_JSON);
        addFunctionName(Functions.GEO_AS_GEO_JSON);
        addFunctionName(Functions.GEO_AS_TEXT);
        addFunctionName(Functions.GEO_BOUNDARY);
        addFunctionName(Functions.GEO_BUFFER);
        addFunctionName(Functions.GEO_CENTROID);
        addFunctionName(Functions.GEO_CONTAINS);
        addFunctionName(Functions.GEO_CONVERT_TO_MULTI);
        addFunctionName(Functions.GEO_CROSSES);
        addFunctionName(Functions.GEO_DIFFERENCE);
        addFunctionName(Functions.GEO_DISJOINT);
        addFunctionName(Functions.GEO_DISTANCE);
        addFunctionName(Functions.GEODESIC_DISTANCE);
        addFunctionName(Functions.GEO_ENVELOPE);
        addFunctionName(Functions.GEO_EQUALS);
        addFunctionName(Functions.GEO_TYPE);
        addFunctionName(Functions.GEO_INTERSECTION);
        addFunctionName(Functions.GEO_INTERSECTS);
        addFunctionName(Functions.GEO_IS_EMPTY);
        addFunctionName(Functions.GEO_IS_MEASURED);
        addFunctionName(Functions.GEO_IS_SIMPLE);
        addFunctionName(Functions.GEO_IS_SIMPLE_RELAXED);
        addFunctionName(Functions.GEO_MAKE_SIMPLE);
        addFunctionName(Functions.GEO_MAKE_SIMPLE_RELAXED);
        addFunctionName(Functions.GEO_OVERLAPS);
        addFunctionName(Functions.GEO_REDUCE_FROM_MULTI);
        addFunctionName(Functions.GEO_SRID);
        addFunctionName(Functions.GEO_SYM_DIFFERENCE);
        addFunctionName(Functions.GEO_TOUCHES);
        addFunctionName(Functions.GEO_UNION);
        addFunctionName(Functions.GEO_WITHIN);
        addFunctionName(Functions.GEO_PROJECT);
        addFunctionName(Functions.DEGREES_TO_DECIMAL);
        addFunctionName(Functions.AREA);
        addFunctionName(Functions.PERIMETER);
        addFunctionName(Functions.LINE_DISTANCE);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result;
        OGCGeometry geometry = GeoUtils.createGeometry(parameters[0]);

        switch (functionName) {
            case Functions.GEO_NEW: result = JsonUtils.createObject(geometry.asGeoJson());break;
            case Functions.GEO_AS_BINARY: result = geometry.asBinary(); break;
            case Functions.GEO_AS_GEO_JSON: result = geometry.asGeoJson(); break;
            case Functions.GEO_AS_JSON: result = geometry.asJson(); break;
            case Functions.GEO_AS_TEXT: result = geometry.asText(); break;
            case Functions.GEO_BOUNDARY: result = geometry.boundary(); break;
            case Functions.GEO_BUFFER: {
                try {
                    checkNumberAndType(functionName, parameters, 2, Object.class, Double.class);
                    result = geometry.buffer((Double)parameters[1]);
                }catch (Exception ex){
                    checkNumberAndType(functionName, parameters, 3, Object.class, Double.class, Long.class);
                    result = geometry.buffer((Double)parameters[1], ((Long) parameters[2]).intValue());
                }
                break;
            }
            case Functions.GEO_CENTROID: result = geometry.centroid(); break;
            case Functions.GEO_CONTAINS: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.contains(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_CONVERT_TO_MULTI: result = geometry.convertToMulti(); break;
            case Functions.GEO_CROSSES: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.crosses(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_DIFFERENCE: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.difference(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_DISJOINT: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.disjoint(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_DISTANCE: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.distance(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_ENVELOPE: result = geometry.envelope(); break;
            case Functions.GEO_EQUALS: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.Equals(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_INTERSECTION: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.intersection(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_INTERSECTS: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.intersects(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_IS_EMPTY: result = geometry.isEmpty(); break;
            case Functions.GEO_IS_MEASURED: result = geometry.isMeasured(); break;
            case Functions.GEO_IS_SIMPLE: result = geometry.isSimple(); break;
            case Functions.GEO_IS_SIMPLE_RELAXED: result = geometry.isSimpleRelaxed(); break;
            case Functions.GEO_MAKE_SIMPLE: result = geometry.makeSimple(); break;
            case Functions.GEO_MAKE_SIMPLE_RELAXED: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Boolean.class);
                result = geometry.makeSimpleRelaxed((Boolean)parameters[1]);
                break;
            }
            case Functions.GEO_OVERLAPS: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.overlaps(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_REDUCE_FROM_MULTI: result = geometry.reduceFromMulti(); break;
            case Functions.GEO_SRID: result = geometry.SRID(); break;
            case Functions.GEO_SYM_DIFFERENCE: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.symDifference(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_TOUCHES: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.touches(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_TYPE: result = geometry.geometryType(); break;
            case Functions.GEO_UNION: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.union(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEO_WITHIN: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = geometry.within(GeoUtils.createGeometry(parameters[1]));
                break;
            }
            case Functions.GEODESIC_DISTANCE: {
                checkNumberAndType(functionName, parameters, 2, Object.class, Object.class);
                result = GeometryEngine.geodesicDistanceOnWGS84((Point) geometry.centroid().getEsriGeometry(),
                        (Point) GeoUtils.createGeometry(parameters[1]).centroid().getEsriGeometry());
                break;
            }
            case Functions.DEGREES_TO_DECIMAL: {
                checkNumberAndType(functionName, parameters, 1, String.class);
                result = GeoUtils.degreesToDouble((String) parameters[0]);
                break;
            }
            case Functions.AREA: {
                if(geometry instanceof OGCPolygon) {
                    result = GeoUtils.calculateArea((OGCPolygon) geometry);
                } else {
                    throw new HCJFRuntimeException("Area function expecting a polygon as parameter");
                }
                break;
            }
            case Functions.PERIMETER: {
                if(geometry instanceof OGCPolygon) {
                    result = GeoUtils.calculatePerimeter((OGCPolygon) geometry);
                } else {
                    throw new HCJFRuntimeException("Perimeter function expecting a polygon as parameter");
                }
                break;
            }
            case Functions.LINE_DISTANCE: {
                if(geometry instanceof OGCLineString) {
                    result = GeoUtils.calculateLineDistance((OGCLineString) geometry);
                } else {
                    throw new HCJFRuntimeException("Line distance function expecting a line string as parameter");
                }
                break;
            }
            default: throw new HCJFRuntimeException("Unrecognized get function: %s", functionName);
        }

        if(result instanceof OGCGeometry) {
            result = JsonUtils.createObject(((OGCGeometry)result).asGeoJson());
        }

        return result;
    }

}
