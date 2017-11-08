package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BurstImagePreviewGallery extends MyGallery {
    BurstViewController mController;

    public BurstImagePreviewGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BurstImagePreviewGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setController(BurstViewController controller) {
        this.mController = controller;
    }

    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (h != oldH) {
            BurstImagePreviewAdapter adapter = (BurstImagePreviewAdapter) getAdapter();
            if (adapter != null) {
                adapter.updateViewForImageHeight(h);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        invalidate();
        return super.onTouchEvent(event);
    }

    public void trackMotionScroll(int deltaX) {
        super.trackMotionScroll(deltaX);
        this.mController.trackScroll(deltaX);
    }

    protected void onFinishedMovement() {
        super.onFinishedMovement();
        this.mController.onFinishedMovement();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mController.onConfigurationChanged(true);
    }
}
