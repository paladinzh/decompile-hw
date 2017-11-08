package com.huawei.keyguard.view.widget;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import com.huawei.keyguard.view.effect.LensFlareRenderer;

public class LensFlareView extends GLSurfaceView {
    private BroadcastReceiver mReceiver;
    private LensFlareRenderer mRenderer;

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    protected void onAttachedToWindow() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        getContext().registerReceiver(this.mReceiver, filter);
        super.onAttachedToWindow();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        this.mRenderer.onVisibilityChanged(visibility);
        super.onVisibilityChanged(changedView, visibility);
    }

    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(this.mReceiver);
        this.mRenderer.clear();
        super.onDetachedFromWindow();
    }
}
