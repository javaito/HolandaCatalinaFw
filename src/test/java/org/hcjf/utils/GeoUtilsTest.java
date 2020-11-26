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

    @Test
    public void test2() {
        String s = "01030000000100000005000000FFFFFF6F30514DC0BA0EC2EB703A41C0FFFFFF6F30514DC0BA0EC2EB703A41C0FFFFFF6F30514DC0BA0EC2EB703A41C0FFFFFF6F30514DC0BA0EC2EB703A41C0FFFFFF6F30514DC0BA0EC2EB703A41C0";
        byte[] be = Strings.hexToBytes(s, false);
        byte[] le = Strings.hexToBytes(s, true);
        String s1 = "01010000000000000000003e400000000000002440";
        byte[] be1 = Strings.hexToBytes(s1, false);
        byte[] le1 = Strings.hexToBytes(s1, true);
        OGCGeometry geometry = GeoUtils.createGeometry(be);
        System.out.println();
    }

}
