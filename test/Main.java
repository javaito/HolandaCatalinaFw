import junit.framework.TestSuite;
import org.hcjf.io.net.HttpClientStressTest;
import org.hcjf.io.net.http.*;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.junit.runners.Suite;

/**
 * Created by javaito on 15/1/2016.
 */
@Suite.SuiteClasses({
        HttpClientStressTest.class
})
public class Main extends TestSuite {

    public static void main(String[] args) {
        new Main();
    }

}
