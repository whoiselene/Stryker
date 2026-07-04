package com.stryker.net;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Broadcast extends WebSocketServer {
    private final Set<WebSocket> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Broadcast(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        System.out.println("[BROADCAST] WebSocket server started successfully on port: " + getPort());
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("[BROADCAST] New connection established from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("[BROADCAST] Connection closed for: " + conn.getRemoteSocketAddress() + " (code: " + code + ")");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[BROADCAST] Received control command from client: " + message);
        // Let the client trigger controls if they want to. E.g. "RESET", "PAUSE"
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[BROADCAST] Error occurred: " + ex.getMessage());
        if (conn != null) {
            connections.remove(conn);
        }
    }

    /**
     * Broadcasts a JSON telemetry state frame to all active web clients.
     */
    public void broadcastTelemetry(String telemetryJson) {
        if (connections.isEmpty()) {
            return;
        }
        for (WebSocket conn : connections) {
            if (conn.isOpen()) {
                conn.send(telemetryJson);
            }
        }
    }
}
