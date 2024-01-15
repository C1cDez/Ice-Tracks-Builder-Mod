package com.cicdez.iceboattrack;

public class PlanePoint {
    public final double x, y;
    public PlanePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PlanePoint add(PlanePoint point) {
        return new PlanePoint(this.x + point.x, this.y + point.y);
    }
    public PlanePoint subtract(PlanePoint point) {
        return new PlanePoint(this.x - point.x, this.y - point.y);
    }
    public PlanePoint multiply(double scale) {
        return new PlanePoint(this.x * scale, this.y * scale);
    }
    public PlanePoint divide(double scale) {
        return new PlanePoint(this.x / scale, this.y / scale);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlanePoint) {
            PlanePoint point = (PlanePoint) obj;
            return point.x == this.x && point.y == this.y;
        } else return false;
    }

    @Override
    public String toString() {
        return "(" + x + ";" + y + ")";
    }
}
