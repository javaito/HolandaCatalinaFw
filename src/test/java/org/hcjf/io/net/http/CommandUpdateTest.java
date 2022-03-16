package org.hcjf.io.net.http;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.command.CommandUpdateLayerInterface;
import org.hcjf.layers.crud.command.ResourceCommandLayer;
import org.hcjf.layers.crud.command.ResourceCommandLayerInterface;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CommandUpdateTest {

    private Gson gson;

    @BeforeClass
    public static void setup() {
        System.setProperty(SystemProperties.Net.Rest.COMMAND_FIELD, "_command");
        System.setProperty(SystemProperties.Net.Rest.COMMANDS_FIELD, "_commands");
        Layers.publishLayer(TestResource.class);
        Layers.publishLayer(TestCommand.class);
        Layers.publishLayer(TestCommand2.class);
    }

    @Before
    public void setupTests() {
        gson = new GsonBuilder().create();
    }

    @Test
    public void testCommand() {
        RestContext rest = new RestContext("/rest");
        HttpRequest req = new HttpRequest("/rest/Test", HttpMethod.PUT);
        String body = "PUT /rest/Test HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 69\r\n" +
                "\r\n" +
                "{\"_command\":{\"command\":\"cmd1\",\"payload\":{\"key\":\"Command 1 payload\"}}}";

        req.addData(body.getBytes(StandardCharsets.UTF_8));
        HttpResponse response = rest.onContext(req);

        Map<String, Object> result = Introspection.toMap(JsonUtils.createObject(new String(response.getBody(), StandardCharsets.UTF_8)));
        assertCommandResult(result, "Command 1 payload");
    }

    @Test
    public void testCommands() {
        RestContext rest = new RestContext("/rest");
        HttpRequest req = new HttpRequest("/rest/Test", HttpMethod.PUT);
        String body = "PUT /rest/Test HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 129\r\n" +
                "\r\n" +
                "{\"_commands\":[{\"command\":\"cmd1\",\"payload\":{\"key\":\"Command 1 payload\"}},{\"command\":\"cmd2\",\"payload\":{\"key\":\"Command 2 payload\"}}]}";

        req.addData(body.getBytes(StandardCharsets.UTF_8));
        HttpResponse response = rest.onContext(req);

        List<Map<String, Object>> result = gson.fromJson(new String(response.getBody(), StandardCharsets.UTF_8), new TypeToken<List<Map<String, Object>>>(){}.getType());
        Assert.assertEquals(2, result.size());
        Map<String, Object> result1 = result.get(0);
        assertCommandResult(result1, "Command 1 payload");
        Map<String, Object> result2 = result.get(1);
        assertCommandResult(result2, "Command 2 payload");
    }

    private void assertCommandResult(Map<String, Object> result, String expectedPayload) {
        Assert.assertTrue(result.containsKey("requestPayload"));
        Assert.assertTrue(result.containsKey("response"));
        Map<String, Object> payload = (Map<String, Object>) result.get("requestPayload");
        Assert.assertNotNull(payload);
        Assert.assertTrue(payload.containsKey("key"));
        Assert.assertEquals(expectedPayload, payload.get("key"));
        Assert.assertEquals("OK", result.get("response"));
    }

    public static class TestResource extends Layer implements CommandUpdateLayerInterface {
        public TestResource() {
            super("Test");
        }
    }

    public static class TestCommand extends ResourceCommandLayer implements ResourceCommandLayerInterface {
        public TestCommand() {
            super("Test");
        }

        @Override
        protected String getCommandName() {
            return "cmd1";
        }

        @Override
        public Map<String, Object> execute(Object payload) {
            Map<String, Object> payloadMap = Introspection.toMap(payload);
            return Map.of("requestPayload", payloadMap, "response", "OK");
        }
    }

    public static class TestCommand2 extends ResourceCommandLayer implements ResourceCommandLayerInterface {
        public TestCommand2() {
            super("Test");
        }

        @Override
        protected String getCommandName() {
            return "cmd2";
        }

        @Override
        public Map<String, Object> execute(Object payload) {
            Map<String, Object> payloadMap = Introspection.toMap(payload);
            return Map.of("requestPayload", payloadMap, "response", "OK");
        }
    }
}
