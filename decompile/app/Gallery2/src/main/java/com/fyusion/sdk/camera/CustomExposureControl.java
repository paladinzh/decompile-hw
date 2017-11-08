package com.fyusion.sdk.camera;

import com.fyusion.sdk.camera.impl.e;

/* compiled from: Unknown */
public class CustomExposureControl<TGT extends e> {
    private TGT a = null;

    public CustomExposureControl(TGT tgt) {
        this.a = tgt;
    }

    public void decreaseBrightness(int i) {
        if (this.a != null) {
            this.a.decreaseBrightness(i);
        }
    }

    public void increaseBrightness(int i) {
        if (this.a != null) {
            this.a.increaseBrightness(i);
        }
    }
}
