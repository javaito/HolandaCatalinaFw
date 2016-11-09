package org.hcjf.plugins;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class PluginClassCompiled extends SimpleJavaFileObject {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public PluginClassCompiled(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return baos;
    }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }

}
