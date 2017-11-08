package com.huawei.gallery.anim;

import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

public class ProgressAnimation extends Animation {
    private float mCurrent = 0.0f;

    public float get() {
        return this.mCurrent;
    }

    protected void onCalculate(float progress) {
        this.mCurrent = Utils.clamp(progress, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
    }
}
