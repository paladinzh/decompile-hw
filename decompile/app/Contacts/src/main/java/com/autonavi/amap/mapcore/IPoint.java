package com.autonavi.amap.mapcore;

public class IPoint {
    public int x;
    public int y;

    public IPoint(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public Object clone() {
        try {
            return (IPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
