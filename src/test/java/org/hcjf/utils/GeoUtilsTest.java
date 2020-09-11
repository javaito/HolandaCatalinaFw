package org.hcjf.utils;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.junit.Assert;
import org.junit.Test;

public class GeoUtilsTest {

    @Test
    public void testCreateGeometryFromGeoJson() {
        String json = "{" +
                "  \"type\": \"FeatureCollection\"," +
                "  \"features\": [" +
                "    {" +
                "      \"type\": \"Feature\"," +
                "      \"properties\": {" +
                "        \"marker-color\": \"#7e7e7e\"," +
                "        \"marker-size\": \"medium\"," +
                "        \"marker-symbol\": \"\"," +
                "        \"_radius\": 2000" +
                "      }," +
                "      \"geometry\": {" +
                "        \"type\": \"Point\"," +
                "        \"coordinates\": [" +
                "          -62.22656249999999," +
                "          -15.961329081596647" +
                "        ]" +
                "      }" +
                "    }" +
                "  ]" +
                "}";

        String json2 = "{" +
                "  \"type\": \"FeatureCollection\"," +
                "  \"features\": [" +
                "    {" +
                "      \"type\": \"Feature\"," +
                "      \"properties\": {}," +
                "      \"geometry\": {" +
                "        \"type\": \"Point\"," +
                "        \"coordinates\": [" +
                "          -62.226465940475464," +
                "          -15.961349712021098" +
                "        ]" +
                "      }" +
                "    }" +
                "  ]" +
                "}";

        OGCGeometry geometry = GeoUtils.createGeometry(json);
        OGCGeometry geometry2 = GeoUtils.createGeometry(json2);

        Assert.assertTrue(geometry.contains(geometry2));


    }

}
