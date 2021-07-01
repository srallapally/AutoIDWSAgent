package com.forgerock.autoid.remote;

import com.forgerock.autoid.icf.ConnectorDriver;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Properties;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

public class Main {
    private static final String PROP_PORT = "connectorserver.port";
    private static final String PROP_BUNDLE_DIR = "connectorserver.bundleDir";
    private static final String PROP_LIB_DIR = "connectorserver.libDir";
    private static final String PROP_KEY = "connectorserver.key";
    private static final String PROP_FACADE_LIFETIME = "connectorserver.maxFacadeLifeTime";
    private static final String PROP_LOGGER_CLASS = "connectorserver.loggerClass";
    private static final String PROP_REMOTE_URL = "connectorserver.url";
    private static final String PROP_AGENT_ID = "cconnectorserver.hostId";
    private static final String IIQ_BUNDLE_NAME="iiq.bundle";
    private static final String IIQ_BUNDLE_VER="iiq.bundle.version";
    private static final String PROP_SSL = "connectorserver.usessl";
    private static final String PROP_SSL_TRUSTSTORE_TYPE = "connectorserver.trustStoreType";
    private static final String PROP_SSL_TRUSTSTORE_FILE = "connectorserver.trustStoreFile";
    private static final String PROP_SSL_TRUSTSTORE_PASS = "connectorserver.trustStorePass";
    private static final String PROP_SSL_KEYSTORE_TYPE = "connectorserver.keyStoreType";
    private static final String PROP_SSL_KEYSTORE_FILE = "connectorserver.keyStoreFile";
    private static final String PROP_SSL_KEYSTORE_PASS = "connectorserver.keyStorePass";
    private static final String PROP_SSL_KEY_PASS = "connectorserver.keyPass";

    private static final String DEFAULT_LOG_SPI = "org.identityconnectors.common.logging.StdOutLogger";
    private static final String DEFAULT_BUNDLE_DIR = "connectors";
    private static final String DEFAULT_LIB_DIR = "lib";

    private static Log log; // Initialized lazily to avoid early initialization.
    private static AutoIDWebSocketClient connectorServer;
    private static ConnectorDriver connectorDriver;
    private static String serverUri;
    private static String agentId;
    private String propertiesFileName = "/Users/sanjay.rallapally/IdeaProjects/AutoIDWSAgent/src/main/resources/agent.properties";
    public static void main(String[] arguments) throws IOException {
        if (arguments.length == 0 || arguments.length % 2 != 1) {
            return;
        }

        String cmd = arguments[0];
        if (cmd.equalsIgnoreCase("-run")) {
            String propertiesFileName = getArgumentValue(arguments, "-properties");
            if (isNotBlank(propertiesFileName)) {
                Properties properties = IOUtil.loadPropertiesFile(propertiesFileName);
                try {
                    run(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Press q to shutdown.");
                char c;
                // read characters
                do {
                    c = (char) br.read();
                } while (c != 'q');
                try {
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                destroy();

            }
        }
    }

    private static void run (Properties properties) throws Exception {
        loadProperties(properties);
        if (connectorServer != null) {
            connectorServer.startClient();
        }

    }
    private static void loadProperties(Properties properties) throws MalformedURLException {
        if (Thread.currentThread().getContextClassLoader() == null) {
            getLog().warn("Context class loader is null, working around");
            Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
        }
        serverUri = getProperty(properties,PROP_REMOTE_URL);
        String bundleDirStr = getProperty(properties, PROP_BUNDLE_DIR);
        if (bundleDirStr == null) {
            bundleDirStr = DEFAULT_BUNDLE_DIR;
        }
        agentId = getProperty(properties,PROP_AGENT_ID);
        connectorDriver = new ConnectorDriver(bundleDirStr);
        String bundleName = getProperty(properties,IIQ_BUNDLE_NAME);
        String bundleVersion = getProperty(properties,IIQ_BUNDLE_VER);
        System.out.println("bundle:"+bundleName+" version:"+bundleVersion);
        connectorServer = new AutoIDWebSocketClient(connectorDriver,serverUri,agentId);
        HashMap<String,String> props = new HashMap<>();
        props.put("baseUrl","http://localhost:9090/11q81");
        props.put("userName","spadmin");
        props.put("password","spadmin");
        connectorServer.setBundleName(bundleName);
        connectorServer.setBundleVersion(bundleVersion);
        connectorServer.setProps(props);
    }
    private static String getArgumentValue(String[] arguments, String keyName) {
        if (arguments.length >= 2) {
            for (int i = 0; i < arguments.length - 1; i++) {
                String name = arguments[i];
                String value = arguments[i + 1];
                if (name.equalsIgnoreCase(keyName)) {
                    return value;
                }
            }
        }
        return null;
    }
    private synchronized static Log getLog() {
        if (log == null) {
            log = Log.getLog(Main.class);
        }
        return log;
    }

    public static void stop() throws Exception {
        if (null != connectorServer) {
            connectorServer.stopClient();
        }
    }

    public static void destroy() {

    }

    private static String getProperty(Properties properties, String name) {
        return System.getProperty(name) != null
                ? System.getProperty(name)
                : properties.getProperty(name);
    }
}