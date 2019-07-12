package org.hcjf.utils;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.hcjf.errors.HCJFRuntimeException;

import java.nio.ByteBuffer;
import java.util.Map;

public class GeoUtils {

    public static final OGCGeometry createGeometry(Object object) {
        OGCGeometry geometry;
        if(object instanceof byte[]) {
            geometry = OGCGeometry.fromBinary(ByteBuffer.wrap((byte[])object));
        } else if(object instanceof String) {
            geometry = OGCGeometry.fromText((String)object);
        } else if(object instanceof Map) {
            geometry = OGCGeometry.fromGeoJson(JsonUtils.toJsonTree(object).toString());
        } else {
            throw new HCJFRuntimeException("Illegal argument exception, unsupported geom data type: %s", object.getClass());
        }
        return geometry;
    }

}
