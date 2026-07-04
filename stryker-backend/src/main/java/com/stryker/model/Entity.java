package com.stryker.model;

public class Entity {
    private String id;
    private String type; // "PLAYER" or "BALL"
    private String team; // "alpha", "beta", or "none"
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double theta; // current heading angle in radians
    private double targetX; // target tactical x for players
    private double targetY; // target tactical y for players
    private double radius;
    private String name;

    public Entity(String id, String type, String team, double x, double y, double radius, String name) {
        this.id = id;
        this.type = type;
        this.team = team;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.name = name;
        this.theta = 0.0;
        this.targetX = x;
        this.targetY = y;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getTeam() { return team; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }
    public double getTheta() { return theta; }
    public void setTheta(double theta) { this.theta = theta; }
    public double getTargetX() { return targetX; }
    public void setTargetX(double targetX) { this.targetX = targetX; }
    public double getTargetY() { return targetY; }
    public void setTargetY(double targetY) { this.targetY = targetY; }
    public double getRadius() { return radius; }
    public String getName() { return name; }

    public void updatePosition() {
        this.x += this.vx;
        this.y += this.vy;
    }

    public void updateTheta() {
        if (Math.abs(vx) > 0.001 || Math.abs(vy) > 0.001) {
            this.theta = Math.atan2(vy, vx);
        }
    }

    public double distanceSq(Entity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }
}
