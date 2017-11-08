package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.tint.TintImageView;

public class AlphaOptimizedImageView extends TintImageView {
    public AlphaOptimizedImageView(Context context) {
        this(context, null);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
