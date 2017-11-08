package com.fyusion.sdk.camera;

import android.annotation.TargetApi;
import com.huawei.watermark.manager.parse.WMElement;

@TargetApi(21)
/* compiled from: Unknown */
public class MeteringArea {
    private static final String TAG = "expma";
    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;
    private float weight;

    /* compiled from: Unknown */
    public enum Preset {
        CENTER_ONLY,
        DIAMOND,
        CENTER_AND_CORNERS
    }

    public MeteringArea(int i, int i2, int i3, int i4) {
        checkValues(i, i2, i3, i4);
        this.positionX = i;
        this.positionY = i2;
        this.sizeX = i3;
        this.sizeY = i4;
        this.weight = WMElement.CAMERASIZEVALUE1B1;
    }

    public MeteringArea(int i, int i2, int i3, int i4, float f) {
        checkValues(i, i2, i3, i4);
        this.positionX = i;
        this.positionY = i2;
        this.sizeX = i3;
        this.sizeY = i4;
        this.weight = f;
        if ((f < 0.0f ? 1 : null) != null || f > WMElement.CAMERASIZEVALUE1B1) {
            throw new IllegalArgumentException("Invalid metering-area values supplied");
        }
    }

    private void checkValues(int i, int i2, int i3, int i4) {
        if (i < 0 || i > 100 || i2 < 0 || i2 > 100 || i3 < 0 || i3 > 100 || i4 < 0 || i4 > 100) {
            throw new IllegalArgumentException("Invalid metering-area values supplied");
        }
    }
}
