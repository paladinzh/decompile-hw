package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.google.android.gms.R;

public class FrameLayoutWithOverlay extends FrameLayout {
    private final AlphaTouchInterceptorOverlay mOverlay;

    public FrameLayoutWithOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOverlay = new AlphaTouchInterceptorOverlay(context);
        this.mOverlay.setId(R.id.alpha_touch_interceptor_overlay);
        addView(this.mOverlay);
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        this.mOverlay.bringToFront();
    }

    protected void setAlphaLayer(View layer) {
        this.mOverlay.setAlphaLayer(layer);
    }
}
