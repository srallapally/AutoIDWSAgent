package com.forgerock.autoid.remote;

import com.forgerock.autoid.icf.ConnectorDriver;
import com.forgerock.autoid.util.Message;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketPingPongListener;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AutoIDSocket extends WebSocketAdapter implements WebSocketPingPongListener {
    private static Logger logger = LoggerFactory.getLogger(AutoIDSocket.class);
    private final CountDownLatch closureLatch = new CountDownLatch(1);
    private Session session;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ConnectorDriver connectorDriver;
    private String agentId;
    private String bundleName;
    private String bundleVersion;
    private HashMap<String,String> props;
    //private final AutoIDWorker router;
    private String sessionId;
    private final Map<String, AutoIDWebSocketSession> sessions = new ConcurrentHashMap<>();

    public AutoIDSocket (ConnectorDriver connectorDriver, String agentId, String bundleName, String bundleVersion, HashMap<String,String> props){
        this.connectorDriver = connectorDriver;
        this.agentId = agentId;
        this.bundleName = bundleName;
        this.bundleVersion = bundleVersion;
        this.props = props;
        //router = new AutoIDWorker(connectorDriver);
    }
    public void onWebSocketConnect(Session sess) {
        this.session = sess;
        super.onWebSocketConnect(sess);
        //sessionId = UUID.randomUUID().toString();
        //final AutoIDWebSocketSession webSocketSession = new AutoIDWebSocketSession(sessionId, sess,agentId);
        logger.debug("Socket Connected: ");
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason) {
        super.onWebSocketClose(statusCode, reason);
        //router.onWebSocketClose(sessionId, statusCode, reason);
    }

    @Override
    public void onWebSocketText(final String message) {
        System.out.println("I am came here first");
        //router.onWebSocketText(message);
        Gson gson = new Gson();
        Message m = gson.fromJson(message, Message.class);
        if(m.op == Message.OP_APPROVAL){
            System.out.println("Do Approvals");
            if(doApprovals()){
                System.out.println("==Done==");
            }
        }
        if(m.op == Message.OP_ACK){
            System.out.println("Ping Acked "+message);
            sessionId = m.sessionId;
            if(this.captureSession(session,agentId)){
                System.out.println("Session synched");
                //if(doApprovals()){
                //    System.out.println("==Done==");
                //}
            } else {
                try {
                    disconnect(m.sessionId,"Unable to synch sessions");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException {
        System.out.println("Awaiting closure from remote");
        closureLatch.await();
    }

    @Override
    public void onWebSocketPing(ByteBuffer byteBuffer) {
        final byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.duplicate().get(bytes);
        System.out.println("Received Ping message: " + new String(bytes));
    }

    @Override
    public void onWebSocketPong(ByteBuffer byteBuffer) {
        final byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.duplicate().get(bytes);
        System.out.println("Received Pong message: " + new String(bytes));
    }

    public Boolean captureSession(Session session, String agentId) {
        //final String sessionId = session.getSessionId();
        if(!(null == this.sessionId)) {
            System.out.println("My session is::"+this.sessionId);
            AutoIDWebSocketSession webSocketSession = new AutoIDWebSocketSession(sessionId, session,agentId);
            sessions.put(sessionId, webSocketSession);
            System.out.println("Sessions are in synch");
            return true;
        } else {
            System.out.println("Session synch failed");
        }
        return false;
    }

    private AutoIDWebSocketSession getSessionOrFail(final String sessionId) {
        final AutoIDWebSocketSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalStateException("Session was not found for the sessionId: " + sessionId);
        }
        return session;
    }

    public void sendMessage(final String sessionId, final Message sendMessage) throws IOException {
        final AutoIDWebSocketSession session = getSessionOrFail(sessionId);
        Gson gson = new Gson();
        session.sendString(gson.toJson(sendMessage,Message.class));
        //sendMessage.send(session);
    }

    public void disconnect(final String sessionId, final String reason) throws IOException {
        final AutoIDWebSocketSession session = getSessionOrFail(sessionId);
        session.close(reason);
        sessions.remove(sessionId);
    }

    private Boolean doApprovals(){
        //connectorDriver.configureConnector(bundleName, bundleVersion,props);
        return true;
    }
}
