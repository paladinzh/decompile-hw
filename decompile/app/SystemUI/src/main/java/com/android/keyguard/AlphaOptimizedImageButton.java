package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class AlphaOptimizedImageButton extends ImageButton {
    public AlphaOptimizedImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
