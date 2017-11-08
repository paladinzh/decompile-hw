package com.spe3d;

public class Spe3DNativeLib {
    public static native void addTouchPoint(long j, int i, int i2, float f, float f2);

    public static native void clearContents();

    public static native int[] getClearColor();

    public static native String[] getObjectNames();

    public static native void home();

    public static native void init(int i, int i2);

    public static native void keyboardDown(int i);

    public static native void keyboardUp(int i);

    public static native void loadObject(String str);

    public static native void loadObjectWithName(String str, String str2);

    public static native void loadObjectWithServiceHost(ServiceHostWrapper serviceHostWrapper, String str, String str2);

    public static native void mouseButtonPressEvent(float f, float f2, int i);

    public static native void mouseButtonReleaseEvent(float f, float f2, int i);

    public static native void mouseMoveEvent(float f, float f2);

    public static native void setClearColor(int i, int i2, int i3);

    public static native void setLights(Light[] lightArr, int i);

    public static native void setUniformVec3(String str, float f, float f2, float f3);

    public static native void setUniformVec4(String str, float f, float f2, float f3, float f4);

    public static native void step();

    public static native long touchBeganEvent(int i, float f, float f2);

    public static native long touchEndedEvent(int i, float f, float f2, int i2);

    public static native long touchMovedEvent(int i, float f, float f2);

    public static native void unLoadObject(int i);

    public static native void updateState();

    static {
        System.loadLibrary("Spe3DNativeLib");
    }
}
