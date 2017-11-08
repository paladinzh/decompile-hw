package com.android.keyguard;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.accessibility.AccessibilityManager;

class LiftToActivateListener implements OnHoverListener {
    private final AccessibilityManager mAccessibilityManager;
    private boolean mCachedClickableState;

    public LiftToActivateListener(Context context) {
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
    }

    public boolean onHover(View v, MotionEvent event) {
        if (this.mAccessibilityManager.isEnabled() && this.mAccessibilityManager.isTouchExplorationEnabled()) {
            switch (event.getActionMasked()) {
                case 9:
                    this.mCachedClickableState = v.isClickable();
                    v.setClickable(false);
                    break;
                case 10:
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (x > v.getPaddingLeft() && y > v.getPaddingTop() && x < v.getWidth() - v.getPaddingRight() && y < v.getHeight() - v.getPaddingBottom()) {
                        v.performClick();
                    }
                    v.setClickable(this.mCachedClickableState);
                    break;
            }
        }
        v.onHoverEvent(event);
        return true;
    }
}
