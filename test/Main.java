import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

/**
 * Created by javaito on 15/1/2016.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Log.d("", "Hola mundo!!");

        Log.d("", "Hola, excepcion!!", new NullPointerException());

        SystemProperties.get("bla", V -> (V.length() == 4));
    }

}
