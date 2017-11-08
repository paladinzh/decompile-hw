package com.android.settings.colortemper;

public class ColorTemperUtils {
    private static ColorTemperUtils mColorUtils;
    private float b = 1.0f;
    private float g = 1.0f;
    private float r = 1.0f;
    private float x_user_set = 330.0f;
    private float xout = 330.0f;
    private float y_user_set = 330.0f;
    private float yout = 330.0f;

    public static synchronized ColorTemperUtils getInstance() {
        ColorTemperUtils colorTemperUtils;
        synchronized (ColorTemperUtils.class) {
            if (mColorUtils == null) {
                mColorUtils = new ColorTemperUtils();
            }
            colorTemperUtils = mColorUtils;
        }
        return colorTemperUtils;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setOutX(float xout) {
        this.xout = xout;
    }

    public void setOutY(float yout) {
        this.yout = yout;
    }

    public void setUserX(float x_user_set) {
        this.x_user_set = x_user_set;
    }

    public void setUserY(float y_user_set) {
        this.y_user_set = y_user_set;
    }

    public float getR() {
        return this.r;
    }

    public float getG() {
        return this.g;
    }

    public float getB() {
        return this.b;
    }

    public float getX() {
        return this.xout;
    }

    public float getY() {
        return this.yout;
    }

    public float getUserX() {
        return this.x_user_set;
    }

    public float getUserY() {
        return this.y_user_set;
    }
}
