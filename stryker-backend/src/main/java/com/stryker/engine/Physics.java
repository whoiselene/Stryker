package com.stryker.engine;

import com.stryker.model.Entity;

public class Physics {
    public static final double FIELD_WIDTH = 100.0;
    public static final double FIELD_HEIGHT = 60.0;

    public static final double GOAL_Y_MIN = 22.0;
    public static final double GOAL_Y_MAX = 38.0;

    public static final double BALL_FRICTION = 0.985; // Decelerates ball by 1.5% per frame
    public static final double BOUNCE_COEFFICIENT = 0.6; // Coefficient of restitution for walls
    public static final double MIN_SPEED = 0.01;

    /**
     * Checks if a player has intercepted the ball.
     */
    public static boolean intercepts(Entity player, Entity ball) {
        double dx = player.getX() - ball.getX();
        double dy = player.getY() - ball.getY();
        double distSq = dx * dx + dy * dy;
        double minDist = player.getRadius() + ball.getRadius();
        return distSq <= minDist * minDist;
    }

    /**
     * Applies friction to the ball's velocity vector.
     */
    public static void applyFriction(Entity ball) {
        if ("BALL".equals(ball.getType())) {
            double vx = ball.getVx() * BALL_FRICTION;
            double vy = ball.getVy() * BALL_FRICTION;

            if (Math.sqrt(vx * vx + vy * vy) < MIN_SPEED) {
                vx = 0.0;
                vy = 0.0;
            }
            ball.setVx(vx);
            ball.setVy(vy);
        }
    }

    /**
     * Restricts an entity within the field boundaries, handling wall bounces for the ball.
     */
    public static void constrainToBounds(Entity entity) {
        double r = entity.getRadius();
        boolean isBall = "BALL".equals(entity.getType());

        if (isBall) {
            // Ball constraints: special goal checks are done in MatchLoop
            // But if it goes outside goals on the left/right, it bounces.
            if (entity.getX() < r) {
                if (entity.getY() >= GOAL_Y_MIN && entity.getY() <= GOAL_Y_MAX) {
                    // Let it pass through to count as a Goal
                } else {
                    entity.setX(r);
                    entity.setVx(-entity.getVx() * BOUNCE_COEFFICIENT);
                }
            } else if (entity.getX() > FIELD_WIDTH - r) {
                if (entity.getY() >= GOAL_Y_MIN && entity.getY() <= GOAL_Y_MAX) {
                    // Let it pass through to count as a Goal
                } else {
                    entity.setX(FIELD_WIDTH - r);
                    entity.setVx(-entity.getVx() * BOUNCE_COEFFICIENT);
                }
            }

            if (entity.getY() < r) {
                entity.setY(r);
                entity.setVy(-entity.getVy() * BOUNCE_COEFFICIENT);
            } else if (entity.getY() > FIELD_HEIGHT - r) {
                entity.setY(FIELD_HEIGHT - r);
                entity.setVy(-entity.getVy() * BOUNCE_COEFFICIENT);
            }
        } else {
            // Player constraints: clamp strictly to boundaries
            if (entity.getX() < r) {
                entity.setX(r);
                entity.setVx(0.0);
            } else if (entity.getX() > FIELD_WIDTH - r) {
                entity.setX(FIELD_WIDTH - r);
                entity.setVx(0.0);
            }

            if (entity.getY() < r) {
                entity.setY(r);
                entity.setVy(0.0);
            } else if (entity.getY() > FIELD_HEIGHT - r) {
                entity.setY(FIELD_HEIGHT - r);
                entity.setVy(0.0);
            }
        }
    }
}
