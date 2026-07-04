package com.stryker.model;

public final class Position {
    private final double x;
    private final double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distance(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceSq(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    public Position add(double dx, double dy) {
        return new Position(this.x + dx, this.y + dy);
    }

    public Position subtract(Position other) {
        return new Position(this.x - other.x, this.y - other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long tempX = Double.doubleToLongBits(x);
        long tempY = Double.doubleToLongBits(y);
        int result = (int) (tempX ^ (tempX >>> 32));
        result = 31 * result + (int) (tempY ^ (tempY >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("Position{x=%.2f, y=%.2f}", x, y);
    }
}
