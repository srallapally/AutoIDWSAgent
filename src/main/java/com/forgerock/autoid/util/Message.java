package com.forgerock.autoid.util;

public class Message {
    public static final int OP_ACK = 0;
    public static final int OP_LOGIN = 1;
    public static final int OP_ROLE = 2;
    public static final int OP_APPROVAL = 3;
    public static final int OP_CERT = 4;
    public static final int OP_STATUS = 5;
    public static final int OP_DISCONNECT = 6;
    public static final int OP_BROADCAST = 7;
    public static final int OP_PING = 8;

    public int op;
    public String agentId;
    public String sessionId;
}
