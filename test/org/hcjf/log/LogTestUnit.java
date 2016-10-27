package org.hcjf.log;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by javaito on 8/3/2016.
 */
public class LogTestUnit {


    public void testLog() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out, true);

        System.setOut(stream);
        Log.i("Tag", "Hola Mundo!! %s", "Javier");

        Assert.assertTrue("Hola Mundo!! Javier".equals(out.toString()));
        out.reset();
    }

}
