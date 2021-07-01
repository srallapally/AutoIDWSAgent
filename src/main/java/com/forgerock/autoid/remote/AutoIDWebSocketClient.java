package com.forgerock.autoid.remote;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.*;

import com.forgerock.autoid.icf.ConnectorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class AutoIDWebSocketClient {
    private static Logger logger = LoggerFactory.getLogger(AutoIDWebSocketClient.class);
    private ConnectorDriver connectorDriver;
    private WebSocketClient client;
    private URI webSocketUri;
    private String uriStr;
    private Session session;
    private String agentId;
    long connectionTimeoutMillis = 1000;
    private String bundleName;
    private String bundleVersion;
    private HashMap<String,String> props;

    public AutoIDWebSocketClient(ConnectorDriver connectorDriver, String uri,String agentId){
        this.connectorDriver = connectorDriver;
        this.uriStr = uri;
        this.agentId = agentId;
    }
    public void startClient() throws Exception {
        webSocketUri  = new URI(uriStr);
        connect();
    }
    public void stopClient() throws Exception {
        if (client == null) {
            return;
        }
        client.stop();
        client = null;
    }
    public void connect() throws Exception {
        // The socket that receives events
        final AutoIDSocket socket = new AutoIDSocket(this.connectorDriver,this.agentId,this.bundleName,this.bundleVersion,this.props);
        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        client = new WebSocketClient();
        client.start();
        final Future<Session> connect = client.connect(socket, webSocketUri, request);
        try {
            session = connect.get(connectionTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IOException("Failed to connect " + webSocketUri + " due to: " + e, e);
        };
    }

    public Session getSession(){
        return this.session;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    public void setProps(HashMap<String, String> props){
        this.props = props;
    }
}
