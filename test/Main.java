import junit.framework.TestSuite;
import org.hcjf.events.*;
import org.hcjf.io.net.HttpClientStressTest;
import org.hcjf.io.net.http.*;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.junit.Test;
import org.junit.runners.Suite;

import java.nio.file.Paths;

/**
 * Created by javaito on 15/1/2016.
 */
@Suite.SuiteClasses({
        HttpClientStressTest.class
})
public class Main extends TestSuite {

    @Test
    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");

        HttpServer server = new HttpServer(8080);
        server.addContext(new EnumContext("enums"));
        server.start();

//        Events.addEventListener(new TestEventListener());
//        Events.addEventListener(new TestEventListener2());
//
//        Events.sendEvent(new TestEvent());
//        Events.sendEvent(new TestEvent2());
    }

}
