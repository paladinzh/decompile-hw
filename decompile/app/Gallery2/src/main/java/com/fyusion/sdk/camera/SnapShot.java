package com.fyusion.sdk.camera;

/* compiled from: Unknown */
public final class SnapShot {
    private byte[] a;
    private int b;
    private int c;
    private int d;

    public int getHeight() {
        return this.c;
    }

    public byte[] getImageData() {
        return this.a;
    }

    public int getImageFormat() {
        return this.d;
    }

    public int getWidth() {
        return this.b;
    }

    public void setHeight(int i) {
        this.c = i;
    }

    public void setImageData(byte[] bArr) {
        this.a = bArr;
    }

    public void setImageFormat(int i) {
        this.d = i;
    }

    public void setWidth(int i) {
        this.b = i;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SnapShot{");
        if (this.a != null) {
            stringBuilder.append("imageData.length = " + this.a.length);
        }
        stringBuilder.append(", width=" + this.b);
        stringBuilder.append(", height=" + this.c);
        stringBuilder.append(", imageFormat=" + this.d);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
