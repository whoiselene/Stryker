package com.stryker;

import com.stryker.engine.MatchLoop;
import com.stryker.net.Broadcast;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[MAIN] Invalid port specified. Using default: " + DEFAULT_PORT);
            }
        }

        System.out.println("================================================================================");
        System.out.println("                     STRYKER FOOTBALL ENGINE & COLLAGE SEEDER                   ");
        System.out.println("================================================================================");

        // Initialize Broadcast WebSocket Server
        Broadcast broadcastServer = new Broadcast(port);
        broadcastServer.start();

        // Initialize Match simulation loop
        MatchLoop matchLoop = new MatchLoop(broadcastServer);
        matchLoop.start();

        // Add shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SHUTDOWN] Terminating STRYKER Simulation Engine...");
            try {
                matchLoop.stop();
                broadcastServer.stop(1000);
                System.out.println("[SHUTDOWN] Gracefully stopped.");
            } catch (InterruptedException e) {
                System.err.println("[SHUTDOWN] Termination interrupted: " + e.getMessage());
            }
        }));
    }
}
