package org.hcjf.utils;

import com.esri.core.geometry.*;
import com.esri.core.geometry.ogc.*;
import org.hcjf.errors.HCJFRuntimeException;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

public class GeoUtils {

    private static final class Paths {
        private static final String COORDINATES = "coordinates";
        private static final String FEATURES = "features";
        private static final String PROPERTIES_RADIUS = "properties._radius";
        private static final String PROPERTIES_BUFFER = "properties._buffer";
        private static final String GEOMETRY = "geometry";
    }

    /**
     * Try to create a geometry instance from a different kinds of objects.
     * If the object is instance of byte[] then it try to create a geometry from a WKB file format.
     * If the Object is instance of string and has a json format then try to create a geometry from a geojson format
     * else if string has not a json format try to create a geometry from WKT format.
     * If the onject instance of Map then it try to create a geometry form geojson format expressed as map.
     * @param object Instance used to create the geometry instance.
     * @return Geometry instance
     */
    public static final OGCGeometry createGeometry(Object object) {
        OGCGeometry geometry;
        if(object instanceof byte[]) {
            geometry = OGCGeometry.fromBinary(ByteBuffer.wrap((byte[])object));
        } else if(object instanceof String) {
            if(((String)object).startsWith(Strings.START_OBJECT) || ((String)object).startsWith(Strings.START_SUB_GROUP)) {
                geometry = fromGeoJson((Map<String, Object>) JsonUtils.createObject((String)object));
            } else {
                geometry = OGCGeometry.fromText((String) object);
            }
        } else if(object instanceof Map) {
            geometry = fromGeoJson((Map<String,Object>)object);
        } else {
            throw new HCJFRuntimeException("Illegal argument exception, unsupported geom data type: %s", object.getClass());
        }
        return geometry;
    }

    private static OGCGeometry fromGeoJson(Map<String,Object> geoJson) {
        OGCGeometry result = null;
        if(geoJson.containsKey(Paths.COORDINATES)) {
            result = OGCGeometry.fromGeoJson(JsonUtils.toJsonTree(geoJson).toString());
        } else {
            Collection<Map<String, Object>> features = Introspection.resolve(geoJson, Paths.FEATURES);
            OGCGeometry geometry;
            for (Map<String, Object> feature : features) {
                Number buffer = Introspection.resolve(feature, Paths.PROPERTIES_RADIUS);
                if (buffer == null) {
                    buffer = Introspection.resolve(feature, Paths.PROPERTIES_BUFFER);
                }
                geometry = OGCGeometry.fromGeoJson(JsonUtils.toJsonTree(Introspection.resolve(feature, Paths.GEOMETRY)).toString());
                if (buffer != null) {
                    geometry = geometry.buffer(buffer.doubleValue());
                }
                if (result == null) {
                    result = geometry;
                } else {
                    result.union(geometry);
                }
            }
        }
        return result;
    }

    /**
     * Transform the value expressed in degrees in the same value expressed as decimal.
     * @param value Value expressed in degrees.
     * @return Decimal format of the value.
     */
    public static Double degreesToDouble(String value) {
        String[] parts = value.split("([°'\"])");

        String hour = parts[0];
        String minute = parts[1];
        String second = parts[2];
        String sign = parts[3].trim();

        double result = Double.parseDouble(hour) +
             Double.parseDouble(minute) / 60.0 +
             Double.parseDouble(second) / 3600.0;

        if(sign.equals("W")||sign.equals("O")||sign.equals("S")) {
            result *= -1;
        }

        return  result;
    }

    /**
     * Calculates the perimeter of the polygon.
     * @param ogcPolygon Polygon instance.
     * @return Perimeter of the polygon.
     */
    public static Double calculatePerimeter(OGCPolygon ogcPolygon) {
        return calculateLineDistance(ogcPolygon.exteriorRing());
    }

    /**
     * Calculates the distance of the line.
     * @param lineString Line string instance.
     * @return Distance of the line.
     */
    public static Double calculateLineDistance(OGCLineString lineString) {
        Double distance = 0.0;
        Integer numberOfPoints = lineString.numPoints();
        for (int i = 0; i < numberOfPoints - 1; i++) {
            OGCPoint startPoint = lineString.pointN(i);
            OGCPoint endPoint = lineString.pointN(i+1);
            distance += calculateGeodesicDistance(startPoint, endPoint);
        }
        return distance;
    }

    /**
     * Calculates the area of the polygon.
     * @param ogcPolygon Polygon instance.
     * @return Area of the polygon.
     */
    public static Double calculateArea(OGCPolygon ogcPolygon) {
        Polygon polygon = (Polygon) ogcPolygon.getEsriGeometry().copy();
        for (int i = 0; i < polygon.getPointCount(); i++) {
            Point2D point = polygon.getXY(i);
            double[] xy = wgs84ToGoogle(point.x, point.y);
            polygon.setXY(i, xy[0], xy[1]);
        }
        return polygon.calculateArea2D();
    }

    public static Double calculateGeodesicDistance(OGCGeometry geometry1, OGCGeometry geometry2) {
        return GeometryEngine.geodesicDistanceOnWGS84((Point) geometry1.centroid().getEsriGeometry(),
                (Point) geometry2.centroid().getEsriGeometry());
    }

    private static double[] wgs84ToGoogle(double lon, double lat) {
        double x = lon * 20037508.34 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;
        return new double[] {x, y};
    }

    private static double[] googleToWgs84(double x, double y) {
        double lon = (x / 20037508.34) * 180;
        double lat = (y / 20037508.34) * 180;
        lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
        return new double[] {lon, lat};
    }

    public static void main(String[] args) {
    }

}
