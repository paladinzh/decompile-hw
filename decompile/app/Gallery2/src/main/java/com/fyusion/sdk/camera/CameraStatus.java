package com.fyusion.sdk.camera;

/* compiled from: Unknown */
public class CameraStatus {
    private boolean a = false;
    private boolean b;
    private int c;
    private int d;
    private boolean e = true;
    private boolean f = true;
    private int g;
    private int h;
    private int i;
    private FyuseCamera j;
    private int k;
    private int l;

    public CameraStatus(FyuseCamera fyuseCamera) {
        this.j = fyuseCamera;
    }

    public int getCameraOrientation() {
        return this.c;
    }

    public int getCameraRotation() {
        return this.d;
    }

    public int getDisplayOrientation() {
        return this.k;
    }

    public FyuseCamera getFyuseCamera() {
        return this.j;
    }

    public int getImageFormat() {
        return this.l;
    }

    public int getPreviewFPS() {
        return this.i;
    }

    public int getPreviewHeight() {
        return this.h;
    }

    public int getPreviewWidth() {
        return this.g;
    }

    public boolean isBackCamera() {
        return this.e;
    }

    public boolean isMirrorVertically() {
        return this.b;
    }

    public boolean isPortraitMode() {
        return this.f;
    }

    public synchronized boolean isRecording() {
        return this.a;
    }

    public void setBackCamera(boolean z) {
        this.e = z;
    }

    public void setCameraOrientation(int i) {
        this.c = i;
    }

    public void setCameraRotation(int i) {
        this.d = i;
    }

    public void setDisplayOrientation(int i) {
        this.k = i;
    }

    public void setImageFormat(int i) {
        this.l = i;
    }

    public void setMirrorVertically(boolean z) {
        this.b = z;
    }

    public void setPortraitMode(boolean z) {
        this.f = z;
    }

    public void setPreviewFPS(int i) {
        this.i = i;
    }

    public void setPreviewHeight(int i) {
        this.h = i;
    }

    public void setPreviewWidth(int i) {
        this.g = i;
    }

    public synchronized void setRecording(boolean z) {
        this.a = z;
    }
}
