package com.android.contacts.hap.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ShadowView extends View {
    public ShadowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlpha(0.0f);
    }
}
