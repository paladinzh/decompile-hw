package com.android.contacts.widget;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.util.ThemeUtils;
import com.google.android.gms.R;

public class AlphaTouchInterceptorOverlay extends FrameLayout {
    private float mAlpha = 0.0f;
    private View mAlphaLayer;
    private View mInterceptorLayer;

    public AlphaTouchInterceptorOverlay(Context context) {
        super(context);
        this.mInterceptorLayer = new View(context);
        this.mInterceptorLayer.setId(R.id.interceptor_layer);
        this.mInterceptorLayer.setBackgroundResource(ThemeUtils.getSelectableItemBackground(context.getTheme()));
        addView(this.mInterceptorLayer);
        this.mAlphaLayer = this;
    }

    public void setAlphaLayer(View alphaLayer) {
        if (this.mAlphaLayer != alphaLayer) {
            if (this.mAlphaLayer == this) {
                ContactDetailDisplayUtils.setAlphaOnViewBackground(this, 0.0f);
            }
            if (alphaLayer == null) {
                alphaLayer = this;
            }
            this.mAlphaLayer = alphaLayer;
            setAlphaLayerValue(this.mAlpha);
        }
    }

    public void setAlphaLayerValue(float alpha) {
        this.mAlpha = alpha;
        if (this.mAlphaLayer != null) {
            ContactDetailDisplayUtils.setAlphaOnViewBackground(this.mAlphaLayer, this.mAlpha);
        }
    }
}
