package com.stryker.engine;

import com.google.gson.Gson;
import com.stryker.model.Entity;
import com.stryker.model.Position;
import com.stryker.net.Broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MatchLoop {
    private final Broadcast broadcast;
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final Random random = new Random();

    private final List<Entity> players = new ArrayList<>();
    private Entity ball;

    private int alphaScore = 0;
    private int betaScore = 0;
    private long tickCount = 0;

    private Entity possessionPlayer = null;
    private String possessionTeam = "none";

    private String lastEvent = "NONE";
    private String lastEventMsg = "Match is starting!";
    private int eventDisplayTimer = 0;

    // Tactical grids (formations)
    private final double[][] alphaBasePositions = {
        {6, 30},    // GK
        {25, 12}, {25, 24}, {25, 36}, {25, 48}, // Defenders
        {45, 12}, {45, 24}, {45, 36}, {45, 48}, // Midfielders
        {65, 20}, {65, 40}  // Forwards
    };

    private final double[][] betaBasePositions = {
        {94, 30},   // GK
        {75, 12}, {75, 24}, {75, 36}, {75, 48}, // Defenders
        {55, 12}, {55, 24}, {55, 36}, {55, 48}, // Midfielders
        {35, 20}, {35, 40}  // Forwards
    };

    private final String[] alphaNames = {
        "A. Becker", "T. Arnold", "I. Konate", "V. van Dijk", "A. Robertson",
        "A. Mac Allister", "W. Endo", "D. Szoboszlai", "M. Salah", "D. Nunez", "L. Diaz"
    };

    private final String[] betaNames = {
        "Ederson", "K. Walker", "R. Dias", "M. Akanji", "J. Gvardiol",
        "Rodri", "M. Kovacic", "K. De Bruyne", "B. Silva", "E. Haaland", "J. Doku"
    };

    public MatchLoop(Broadcast broadcast) {
        this.broadcast = broadcast;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.gson = new Gson();
        initializeMatch();
    }

    private void initializeMatch() {
        players.clear();
        // Setup Team Alpha (White / White stamp with drop shadow)
        for (int i = 0; i < 11; i++) {
            String id = "player_alpha_" + i;
            Entity p = new Entity(id, "PLAYER", "alpha", alphaBasePositions[i][0], alphaBasePositions[i][1], 1.2, alphaNames[i]);
            players.add(p);
        }
        // Setup Team Beta (Black / Black stamp with drop shadow)
        for (int i = 0; i < 11; i++) {
            String id = "player_beta_" + i;
            Entity p = new Entity(id, "PLAYER", "beta", betaBasePositions[i][0], betaBasePositions[i][1], 1.2, betaNames[i]);
            players.add(p);
        }

        // Setup Ball (Halftone textured)
        ball = new Entity("ball", "BALL", "none", 50.0, 30.0, 0.7, "Ball");
        ball.setVx((random.nextDouble() - 0.5) * 1.5);
        ball.setVy((random.nextDouble() - 0.5) * 1.5);

        possessionPlayer = null;
        possessionTeam = "none";
        lastEvent = "MATCH_START";
        lastEventMsg = "Kick-off! The match has begun.";
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, 16, TimeUnit.MILLISECONDS); // ~60Hz
        System.out.println("[MATCH_LOOP] Simulation engine ticked off at 60Hz.");
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void tick() {
        try {
            tickCount++;

            // Handle Event text decay
            if (eventDisplayTimer > 0) {
                eventDisplayTimer--;
                if (eventDisplayTimer == 0) {
                    lastEvent = "NONE";
                }
            }

            // 1. Check possession intercepts if ball is free
            if (possessionPlayer == null) {
                for (Entity p : players) {
                    if (Physics.intercepts(p, ball)) {
                        setPossession(p, "STEAL", p.getName() + " intercepted the ball!");
                        break;
                    }
                }
            } else {
                // If a player has possession, check if opponent tackles/steals
                for (Entity p : players) {
                    if (!p.getTeam().equals(possessionTeam)) {
                        double dx = p.getX() - possessionPlayer.getX();
                        double dy = p.getY() - possessionPlayer.getY();
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        // Tackle proximity threshold
                        if (dist < (p.getRadius() + possessionPlayer.getRadius() + 0.8)) {
                            if (random.nextDouble() < 0.03) { // 3% chance of tackle per frame
                                setPossession(p, "STEAL", p.getName() + " tackled and stole the ball!");
                                break;
                            }
                        }
                    }
                }
            }

            // 2. Player movements (Tactical Grid AI)
            for (int i = 0; i < players.size(); i++) {
                Entity p = players.get(i);
                boolean hasBall = (p == possessionPlayer);

                if (hasBall) {
                    // Ball carrier runs towards target goal
                    double goalX = "alpha".equals(possessionTeam) ? Physics.FIELD_WIDTH : 0.0;
                    double goalY = 30.0;

                    double dx = goalX - p.getX();
                    double dy = goalY - p.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    // Move towards goal
                    double speed = 0.16; // player run speed
                    p.setVx((dx / dist) * speed);
                    p.setVy((dy / dist) * speed);

                    // Update positions
                    p.updatePosition();
                    p.updateTheta();

                    // Carry the ball along with heading offset
                    double headingX = Math.cos(p.getTheta());
                    double headingY = Math.sin(p.getTheta());
                    ball.setX(p.getX() + headingX * (p.getRadius() + ball.getRadius() - 0.2));
                    ball.setY(p.getY() + headingY * (p.getRadius() + ball.getRadius() - 0.2));
                    ball.setVx(p.getVx());
                    ball.setVy(p.getVy());

                    // Decides to shoot or pass
                    double goalDistance = Math.abs(p.getX() - goalX);
                    if (goalDistance < 32.0 && random.nextDouble() < 0.02) {
                        // SHOT!
                        performShot(p, goalX, goalY);
                    } else if (random.nextDouble() < 0.01) {
                        // PASS!
                        performPass(p);
                    }
                } else {
                    // Standard AI movement based on base position and ball position
                    double targetX;
                    double targetY;

                    int playerIndex = i % 11;
                    double[][] basePos = "alpha".equals(p.getTeam()) ? alphaBasePositions : betaBasePositions;
                    double bx = basePos[playerIndex][0];
                    double by = basePos[playerIndex][1];

                    // Shifts slightly towards ball to form support or press
                    double ballShiftFactor = 0.25;
                    targetX = bx + (ball.getX() - bx) * ballShiftFactor;
                    targetY = by + (ball.getY() - by) * ballShiftFactor;

                    // If ball is loose or opponent has possession, the closest player presses the ball
                    if (isClosestToBall(p)) {
                        targetX = ball.getX();
                        targetY = ball.getY();
                    }

                    double dx = targetX - p.getX();
                    double dy = targetY - p.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist > 0.5) {
                        double speed = 0.13; // default run speed
                        p.setVx((dx / dist) * speed);
                        p.setVy((dy / dist) * speed);
                    } else {
                        p.setVx(p.getVx() * 0.5);
                        p.setVy(p.getVy() * 0.5);
                    }

                    p.updatePosition();
                    p.updateTheta();
                    Physics.constrainToBounds(p);
                }
            }

            // 3. Ball Physics (if not possessed)
            if (possessionPlayer == null) {
                ball.updatePosition();
                ball.updateTheta();
                Physics.applyFriction(ball);
                Physics.constrainToBounds(ball);

                // Check Goal scoring logic
                if (ball.getX() <= 0 && ball.getY() >= Physics.GOAL_Y_MIN && ball.getY() <= Physics.GOAL_Y_MAX) {
                    betaScore++;
                    triggerEvent("GOAL", "GOAL! Team Beta scores through " + getLatelyInvolvedPlayerName() + "!");
                    resetAfterGoal();
                } else if (ball.getX() >= Physics.FIELD_WIDTH && ball.getY() >= Physics.GOAL_Y_MIN && ball.getY() <= Physics.GOAL_Y_MAX) {
                    alphaScore++;
                    triggerEvent("GOAL", "GOAL! Team Alpha scores through " + getLatelyInvolvedPlayerName() + "!");
                    resetAfterGoal();
                } else if (ball.getX() < 0 || ball.getX() > Physics.FIELD_WIDTH) {
                    // Out of bounds, return ball to center or kick back in
                    triggerEvent("OUT_OF_BOUNDS", "Out of bounds! Goal kick awarded.");
                    resetBallToCenter();
                }
            }

            // 4. Send telemetry JSON to listeners
            String telemetry = serializeTelemetry();
            broadcast.broadcastTelemetry(telemetry);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPossession(Entity player, String event, String msg) {
        possessionPlayer = player;
        possessionTeam = player.getTeam();
        triggerEvent(event, msg);
    }

    private void triggerEvent(String event, String msg) {
        lastEvent = event;
        lastEventMsg = msg;
        eventDisplayTimer = 45; // show event tag for 45 ticks (~0.75 seconds)
        System.out.println("[GAME EVENT] " + event + ": " + msg);
    }

    private String getLatelyInvolvedPlayerName() {
        if (possessionPlayer != null) {
            return possessionPlayer.getName();
        }
        // Fallback to random striker
        return random.nextBoolean() ? "Salah" : "Haaland";
    }

    private boolean isClosestToBall(Entity p) {
        double pDist = p.distanceSq(ball);
        for (Entity other : players) {
            if (other.getTeam().equals(p.getTeam()) && other != p) {
                if (other.distanceSq(ball) < pDist) {
                    return false;
                }
            }
        }
        return true;
    }

    private void performPass(Entity passer) {
        // Find a teammate further down the field
        Entity bestTeammate = null;
        double bestX = "alpha".equals(possessionTeam) ? -1 : 999;

        for (Entity teammate : players) {
            if (teammate.getTeam().equals(possessionTeam) && teammate != passer) {
                boolean isBetter = "alpha".equals(possessionTeam) 
                    ? teammate.getX() > passer.getX() 
                    : teammate.getX() < passer.getX();

                if (isBetter) {
                    if (bestTeammate == null || Math.abs(teammate.getX() - passer.getX()) < Math.abs(bestX - passer.getX())) {
                        bestTeammate = teammate;
                        bestX = teammate.getX();
                    }
                }
            }
        }

        // Default to any teammate if none are ahead
        if (bestTeammate == null) {
            for (Entity teammate : players) {
                if (teammate.getTeam().equals(possessionTeam) && teammate != passer) {
                    bestTeammate = teammate;
                    break;
                }
            }
        }

        if (bestTeammate != null) {
            // Kick ball to teammate
            double dx = bestTeammate.getX() - ball.getX();
            double dy = bestTeammate.getY() - ball.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            // Set ball velocity
            double kickForce = 1.6;
            ball.setVx((dx / dist) * kickForce);
            ball.setVy((dy / dist) * kickForce);

            possessionPlayer = null;
            possessionTeam = "none";
            triggerEvent("PASS", passer.getName() + " passed the ball to " + bestTeammate.getName());
        }
    }

    private void performShot(Entity shooter, double goalX, double goalY) {
        // Target center of goal with a minor vertical variance
        double targetY = goalY + (random.nextDouble() - 0.5) * 8.0;
        double dx = goalX - ball.getX();
        double dy = targetY - ball.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        double shotForce = 2.4 + random.nextDouble() * 0.6; // High velocity shot
        ball.setVx((dx / dist) * shotForce);
        ball.setVy((dy / dist) * shotForce);

        possessionPlayer = null;
        possessionTeam = "none";
        triggerEvent("SHOT", shooter.getName() + " fired a SHOT on goal!");
    }

    private void resetAfterGoal() {
        possessionPlayer = null;
        possessionTeam = "none";

        // Reset players to their base coordinates
        for (int i = 0; i < 11; i++) {
            Entity alphaPlayer = players.get(i);
            alphaPlayer.setX(alphaBasePositions[i][0]);
            alphaPlayer.setY(alphaBasePositions[i][1]);
            alphaPlayer.setVx(0.0);
            alphaPlayer.setVy(0.0);

            Entity betaPlayer = players.get(i + 11);
            betaPlayer.setX(betaBasePositions[i][0]);
            betaPlayer.setY(betaBasePositions[i][1]);
            betaPlayer.setVx(0.0);
            betaPlayer.setVy(0.0);
        }

        ball.setX(50.0);
        ball.setY(30.0);
        ball.setVx((random.nextDouble() - 0.5) * 1.5);
        ball.setVy((random.nextDouble() - 0.5) * 1.5);
    }

    private void resetBallToCenter() {
        ball.setX(50.0);
        ball.setY(30.0);
        ball.setVx((random.nextDouble() - 0.5) * 1.5);
        ball.setVy((random.nextDouble() - 0.5) * 1.5);
        possessionPlayer = null;
        possessionTeam = "none";
    }

    private String serializeTelemetry() {
        TelemetryFrame frame = new TelemetryFrame(
            tickCount,
            alphaScore,
            betaScore,
            lastEvent,
            lastEventMsg,
            new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy(), possessionTeam),
            players
        );
        return gson.toJson(frame);
    }

    // Helper classes for serialization
    private static class TelemetryFrame {
        long tick;
        Score score;
        String event;
        String eventMsg;
        BallState ball;
        List<Entity> players;

        TelemetryFrame(long tick, int alpha, int beta, String event, String eventMsg, BallState ball, List<Entity> players) {
            this.tick = tick;
            this.score = new Score(alpha, beta);
            this.event = event;
            this.eventMsg = eventMsg;
            this.ball = ball;
            this.players = players;
        }
    }

    private static class Score {
        int alpha;
        int beta;
        Score(int alpha, int beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
    }

    private static class BallState {
        double x;
        double y;
        double vx;
        double vy;
        String possession;

        BallState(double x, double y, double vx, double vy, String possession) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.possession = possession;
        }
    }
}
