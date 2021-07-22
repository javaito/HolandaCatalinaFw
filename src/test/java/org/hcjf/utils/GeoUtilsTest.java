package org.hcjf.utils;

import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCPolygon;
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

    public void testArea() {
        String geoJson = "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              -68.81357967853546,\n" +
                "              -32.877672480737765\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81384253501892,\n" +
                "              -32.87823112465508\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81294131278992,\n" +
                "              -32.878469898803786\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81258726119994,\n" +
                "              -32.87792927716925\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81357967853546,\n" +
                "              -32.877672480737765\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        OGCGeometry geometry = GeoUtils.createGeometry(geoJson);
        Double area = GeoUtils.calculateArea((OGCPolygon) geometry);
        Double perimeter = GeoUtils.calculatePerimeter((OGCPolygon) geometry);
        System.out.println("Area: " + area);
        System.out.println("Perimeter: " + perimeter);

        geoJson = "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              -68.8150629401207,\n" +
                "              -32.87720168534785\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81507366895676,\n" +
                "              -32.87724223241762\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81361722946167,\n" +
                "              -32.87767473334128\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81385326385498,\n" +
                "              -32.87820634616885\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81521314382553,\n" +
                "              -32.87783692064359\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81524801254272,\n" +
                "              -32.87786620443044\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81383180618286,\n" +
                "              -32.87827167125396\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.81354480981827,\n" +
                "              -32.8776522073036\n" +
                "            ],\n" +
                "            [\n" +
                "              -68.8150629401207,\n" +
                "              -32.87720168534785\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        geometry = GeoUtils.createGeometry(geoJson);
        area = GeoUtils.calculateArea((OGCPolygon) geometry);
        perimeter = GeoUtils.calculatePerimeter((OGCPolygon) geometry);
        System.out.println("Area: " + area);
        System.out.println("Perimeter: " + perimeter);
    }
}
