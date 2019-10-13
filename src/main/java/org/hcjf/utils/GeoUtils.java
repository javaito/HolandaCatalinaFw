package org.hcjf.utils;

import com.esri.core.geometry.ogc.OGCGeometry;
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
}
