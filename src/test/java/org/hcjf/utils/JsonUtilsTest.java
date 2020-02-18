package org.hcjf.utils;

import com.google.gson.JsonElement;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonUtilsTest {

    @Test
    public void testDeductDataType() {
        String json =
                "{" +
                    "\"stringNumber\":\"003\"," +
                    "\"stringLongNumber\":\"123456789132456\"," +
                    "\"longNumber\":123456789132456," +
                    "\"number\":3," +
                    "\"date\":\"1981-02-19 14:00:00\"," +
                    "\"stringBoolean\":\"true\"," +
                    "\"boolean\":true," +
                    "\"uuid\":\"7ce6cff3-1e24-4b87-91a4-f75bce7b61bc\"" +
                "}";
        Map<String,Object> map = (Map<String, Object>) JsonUtils.createObject(json);
        Assert.assertEquals(map.get("stringNumber").getClass(), String.class);
        Assert.assertEquals(map.get("stringNumber"), "003");
        Assert.assertEquals(map.get("number").getClass(), Byte.class);
        Assert.assertEquals(map.get("date").getClass(), Date.class);
        Assert.assertEquals(map.get("stringLongNumber").getClass(), String.class);
        Assert.assertEquals(map.get("longNumber").getClass(), Long.class);
        Assert.assertEquals(map.get("stringBoolean").getClass(), String.class);
        Assert.assertEquals(map.get("boolean").getClass(), Boolean.class);
        Assert.assertEquals(map.get("uuid").getClass(), UUID.class);
    }

    @Test
    public void testJsonTree() throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Map jsonMAp = Map.of("date",dateFormat.parse("2020-02-18 16:34:28.123+00:00"));

        JsonElement element = JsonUtils.toJsonTree(jsonMAp);
        Assert.assertEquals("\"2020-02-18 13:34:28\"", element.getAsJsonObject().get("date").toString());

        JsonElement elementConfig = JsonUtils.toJsonTree(jsonMAp,Map.of(JsonUtils.DATE_FORMAT_ARG,"yyyy-MM-dd'T'HH:mm:ss.SSS"));
        Assert.assertEquals("\"2020-02-18T13:34:28.123\"",elementConfig.getAsJsonObject().get("date").toString());
    }

}
