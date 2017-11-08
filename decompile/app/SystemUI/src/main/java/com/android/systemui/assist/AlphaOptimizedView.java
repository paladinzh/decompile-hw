package com.android.systemui.assist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class AlphaOptimizedView extends View {
    public AlphaOptimizedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
