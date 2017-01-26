import junit.framework.TestSuite;
import org.hcjf.errors.Errors;
import org.hcjf.events.*;
import org.hcjf.io.net.HttpClientStressTest;
import org.hcjf.io.net.http.*;
import org.hcjf.layers.query.Query;
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

        Query query = Query.compile("SELECT * FROM car JOIN CarModel ON CarModel.carId = car.id WHERE car.id = 34");
        System.out.printf("");

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
