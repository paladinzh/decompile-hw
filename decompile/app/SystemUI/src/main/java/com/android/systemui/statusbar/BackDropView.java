package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class BackDropView extends FrameLayout {
    private Runnable mOnVisibilityChangedRunnable;

    public BackDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && this.mOnVisibilityChangedRunnable != null) {
            this.mOnVisibilityChangedRunnable.run();
        }
    }
}
