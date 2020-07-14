package org.hcjf.io.net.http;

import org.hcjf.errors.Errors;
import org.hcjf.properties.SystemProperties;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;

/**
 * @author javaito.
 */
public class HttpsServer extends HttpServer {

    private String keystorePassword;
    private String keyPassword;
    private Path keystoreFilePath;
    private Path trustedCertsFilePath;
    private String keyType;
    private Provider provider;
    private String sslProtocol;

    public HttpsServer() {
        this(SystemProperties.getInteger(SystemProperties.Net.Https.DEFAULT_SERVER_PORT));
    }

    public HttpsServer(Integer port) {
        super(port, true);
        keystorePassword = SystemProperties.get(SystemProperties.Net.Ssl.DEFAULT_KEYSTORE_PASSWORD);
        keyPassword = SystemProperties.get(SystemProperties.Net.Ssl.DEFAULT_KEY_PASSWORD);
        keystoreFilePath = SystemProperties.getPath(SystemProperties.Net.Ssl.DEFAULT_KEYSTORE_FILE_PATH);
        trustedCertsFilePath = SystemProperties.getPath(SystemProperties.Net.Ssl.DEFAULT_TRUSTED_CERTS_FILE_PATH);
        keyType = SystemProperties.get(SystemProperties.Net.Ssl.DEFAULT_KEY_TYPE);
        sslProtocol = SystemProperties.get(SystemProperties.Net.Ssl.DEFAULT_PROTOCOL);
    }

    public static void create(Integer port, Context... contexts) {
        HttpsServer server = new HttpsServer(port);
        for(Context context : contexts) {
            server.addContext(context);
        }
        server.start();
    }

    /**
     * Creates the SSL engine.
     * @return SSL engine instance.
     */
    @Override
    protected SSLEngine getSSLEngine() {
        try {
            SSLContext context = SSLContext.getInstance(getSslProtocol());
            context.init(createKeyManagers(), createTrustManagers(), new SecureRandom());

            SSLSession dummySession = context.createSSLEngine().getSession();
            dummySession.invalidate();

            SSLEngine engine = context.createSSLEngine();
            engine.setUseClientMode(false);
            engine.beginHandshake();
            return engine;
        } catch (Exception ex) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_6), ex);
        }
    }

    /**
     * Return the key store password.
     * @return Key store password.
     */
    public final String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * Set the key store password.
     * @param keystorePassword Key store password.
     */
    public final void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    /**
     * Return the key password.
     * @return Key password.
     */
    public final String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Set the kwy password.
     * @param keyPassword Key password.
     */
    public final void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Return the path where the key store file is.
     * @return Key store file path.
     */
    public final Path getKeystoreFilePath() {
        return keystoreFilePath;
    }

    /**
     * Set the path where the key store file is.
     * @param keystoreFilePath Key store file path.
     */
    public final void setKeystoreFilePath(Path keystoreFilePath) {
        this.keystoreFilePath = keystoreFilePath;
    }

    /**
     * Return the path where trusted certs file is.
     * @return Trusted certs file path.
     */
    public final Path getTrustedCertsFilePath() {
        return trustedCertsFilePath;
    }

    /**
     * Set the path where trusted certs file is.
     * @param trustedCertsFilePath Trusted certs file path.
     */
    public final void setTrustedCertsFilePath(Path trustedCertsFilePath) {
        this.trustedCertsFilePath = trustedCertsFilePath;
    }

    /**
     * Return the key type.
     * @return Key type.
     */
    public final String getKeyType() {
        return keyType;
    }

    /**
     * Set the key type.
     * @param keyType Key type.
     */
    public final void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * Return the ssl protocol.
     * @return Ssl protocol.
     */
    public final String getSslProtocol() {
        return sslProtocol;
    }

    /**
     * Set the ssl protocol.
     * @param sslProtocol Ssl protocol.
     */
    public final void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    /**
     * Return the java security provider.
     * @return Java security provider implementation.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Set the java security provider.
     * @param provider Java security provider implementation.
     */
    public final void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Creates the key managers required to initiate the {@link SSLContext}.
     * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
     * @throws Exception Key managers creation exception
     */
    protected KeyManager[] createKeyManagers() throws Exception {
        KeyStore keyStore = getProvider() == null ?
                KeyStore.getInstance(getKeyType()) : KeyStore.getInstance(getKeyType(), getProvider());
        InputStream keyStoreIS = new FileInputStream(getKeystoreFilePath().toFile());
        try {
            keyStore.load(keyStoreIS, getKeystorePassword().toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, getKeyPassword().toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws Exception Trust manager creation exception
     */
    protected TrustManager[] createTrustManagers() throws Exception {
        KeyStore trustStore = getProvider() == null ? KeyStore.getInstance(getKeyType()) : KeyStore.getInstance(getKeyType(), getProvider());
        InputStream trustStoreIS = new FileInputStream(getTrustedCertsFilePath().toFile());
        try {
            trustStore.load(trustStoreIS, getKeystorePassword().toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }
}
