package com.forgerock.autoid.remote;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class AutoIDWebSocketSession {

    private String sessionId;
    private final Session session;
    private String agentId;

    public AutoIDWebSocketSession(String sessionId, Session session, final String agentId) {
        this.sessionId = sessionId;
        this.session = session;
        this.agentId = agentId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId(){
        return agentId;
    }

    public void sendString(final String message) throws IOException {
        session.getRemote().sendString(message);
    }

    public void sendBinary(final ByteBuffer data) throws IOException {
        session.getRemote().sendBytes(data);
    }

    public void close(final String reason) throws IOException {
        session.close(StatusCode.NORMAL, reason);
    }

    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    public InetSocketAddress getLocalAddress() {
        return session.getLocalAddress();
    }

    public boolean isSecure() {
        return session.isSecure();
    }

}