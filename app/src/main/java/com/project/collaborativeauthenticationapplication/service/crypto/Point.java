package com.project.collaborativeauthenticationapplication.service.crypto;

public class Point {
    private  boolean isZero;
    private BigNumber x;
    private BigNumber y;

    public Point(BigNumber x, BigNumber y, boolean isZero){
        this.x      = x;
        this.y      = y;
        this.isZero = isZero;
    }

    public void setX(BigNumber x) {
        this.x = x;
    }

    public void setY(BigNumber y) {
        this.y = y;
    }

    public void setZero(boolean zero) {
        isZero = zero;
    }

    public BigNumber getX() {
        return x;
    }

    public BigNumber getY() {
        return y;
    }

    public boolean isZero() {
        return isZero;
    }

    public Point(BigNumber x, BigNumber y){
        this(x, y, false);
    }
}
