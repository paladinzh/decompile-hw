package com.huawei.gallery.refocus.wideaperture.ui;

import android.view.animation.AccelerateInterpolator;
import com.android.gallery3d.anim.Animation;

public class RefocusEditorOpenOrQuitEffect extends Animation {
    private float mProgress;
    private int[] mSourcePosition;
    private int[] mTargetPosition;

    public RefocusEditorOpenOrQuitEffect() {
        setInterpolator(new AccelerateInterpolator(2.0f));
        setDuration(300);
    }

    protected void onCalculate(float progress) {
        this.mProgress = progress;
    }

    public void init(int[] sourcePosition, int[] targetPosition) {
        this.mSourcePosition = (int[]) sourcePosition.clone();
        this.mTargetPosition = (int[]) targetPosition.clone();
    }

    public void start() {
        super.start();
    }

    public int[] getCurrentPosition() {
        if (this.mSourcePosition == null || this.mTargetPosition == null) {
            return new int[0];
        }
        int[] currentPosition = new int[4];
        for (int i = 0; i < 4; i++) {
            currentPosition[i] = (int) (((float) this.mSourcePosition[i]) + (((float) (this.mTargetPosition[i] - this.mSourcePosition[i])) * this.mProgress));
        }
        return currentPosition;
    }
}
