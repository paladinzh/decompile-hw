package com.android.gallery3d.ui;

import android.content.Context;
import com.huawei.gallery.util.ResourceUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class ProgressSpinner {
    private static float ROTATE_SPEED_INNER = -0.20571429f;
    private static float ROTATE_SPEED_OUTER = 0.30857143f;
    private long mAnimationTimestamp = -1;
    private final int mHeight;
    private final ResourceTexture mInner;
    private float mInnerDegree = 0.0f;
    private final ResourceTexture mOuter;
    private float mOuterDegree = 0.0f;
    private final int mWidth;

    public ProgressSpinner(Context context) {
        this.mOuter = ResourceUtils.getOuterTexture(context);
        this.mInner = ResourceUtils.getInnerTexture(context);
        this.mWidth = Math.max(this.mOuter.getWidth(), this.mInner.getWidth());
        this.mHeight = Math.max(this.mOuter.getHeight(), this.mInner.getHeight());
    }

    public void startAnimation() {
        this.mAnimationTimestamp = -1;
        this.mOuterDegree = 0.0f;
        this.mInnerDegree = 0.0f;
    }

    public void draw(GLCanvas canvas) {
        long now = AnimationTime.get();
        if (this.mAnimationTimestamp == -1) {
            this.mAnimationTimestamp = now;
        }
        this.mOuterDegree += ((float) (now - this.mAnimationTimestamp)) * ROTATE_SPEED_OUTER;
        this.mInnerDegree += ((float) (now - this.mAnimationTimestamp)) * ROTATE_SPEED_INNER;
        this.mAnimationTimestamp = now;
        if (this.mOuterDegree > 360.0f) {
            this.mOuterDegree -= 360.0f;
        }
        if (this.mInnerDegree < 0.0f) {
            this.mInnerDegree += 360.0f;
        }
        canvas.save(2);
        canvas.rotate(this.mInnerDegree, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        this.mOuter.draw(canvas, (-this.mOuter.getWidth()) / 2, (-this.mOuter.getHeight()) / 2);
        canvas.rotate(this.mOuterDegree - this.mInnerDegree, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        this.mInner.draw(canvas, (-this.mInner.getWidth()) / 2, (-this.mInner.getHeight()) / 2);
        canvas.restore();
    }
}
