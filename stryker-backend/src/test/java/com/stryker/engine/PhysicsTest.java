package com.stryker.engine;

import com.stryker.model.Entity;

public class PhysicsTest {

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Assertion failed: %s. Expected: %.4f, Actual: %.4f", message, expected, actual));
        }
    }

    public void testIntercepts() {
        Entity player = new Entity("p1", "PLAYER", "alpha", 10.0, 10.0, 1.2, "Player 1");
        Entity ball = new Entity("ball", "BALL", "none", 11.0, 10.0, 0.7, "Ball");

        // Distance is 1.0, sum of radii is 1.9 -> should intercept
        assertTrue(Physics.intercepts(player, ball), "Player should intercept ball at distance 1.0");

        // Move ball away
        ball.setX(20.0);
        assertFalse(Physics.intercepts(player, ball), "Player should not intercept ball at distance 10.0");
    }

    public void testApplyFriction() {
        Entity ball = new Entity("ball", "BALL", "none", 50.0, 30.0, 0.7, "Ball");
        ball.setVx(2.0);
        ball.setVy(-1.0);

        Physics.applyFriction(ball);

        // Verify deceleration
        assertTrue(Math.abs(ball.getVx()) < 2.0, "Ball vx should decelerate due to friction");
        assertTrue(Math.abs(ball.getVy()) < 1.0, "Ball vy should decelerate due to friction");
    }

    public void testConstrainToBoundsPlayer() {
        // Player should be clamped strictly to boundary limits
        Entity player = new Entity("p1", "PLAYER", "alpha", -5.0, 70.0, 1.2, "Player 1");
        Physics.constrainToBounds(player);

        assertEquals(player.getRadius(), player.getX(), 0.001, "Player X should be clamped to radius");
        assertEquals(Physics.FIELD_HEIGHT - player.getRadius(), player.getY(), 0.001, "Player Y should be clamped to bounds");
    }

    public void testConstrainToBoundsBallBounce() {
        // Ball should bounce when hitting non-goal walls
        Entity ball = new Entity("ball", "BALL", "none", -1.0, 10.0, 0.7, "Ball");
        ball.setVx(-2.0);

        Physics.constrainToBounds(ball);

        // Should bounce: position set to radius (0.7), velocity flipped and multiplied by bounce coefficient (0.6)
        assertEquals(0.7, ball.getX(), 0.001, "Ball X should bounce off left wall");
        assertEquals(2.0 * Physics.BOUNCE_COEFFICIENT, ball.getVx(), 0.001, "Ball Vx should reverse and be scaled by bounce coeff");
    }
}
