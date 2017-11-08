package com.huawei.gallery.anim;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.GLCanvas;
import com.huawei.watermark.manager.parse.WMElement;

public class AlphaAnimation extends CanvasAnimation {
    private float mCurrentAlpha;
    private final float mEndAlpha;
    private final float mStartAlpha;

    public AlphaAnimation(float from, float to) {
        this.mStartAlpha = from;
        this.mEndAlpha = to;
        this.mCurrentAlpha = from;
    }

    public void apply(GLCanvas canvas) {
        canvas.multiplyAlpha(this.mCurrentAlpha);
    }

    public int getCanvasSaveFlags() {
        return 1;
    }

    protected void onCalculate(float progress) {
        this.mCurrentAlpha = Utils.clamp(this.mStartAlpha + ((this.mEndAlpha - this.mStartAlpha) * progress), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
    }
}
