package com.android.contacts.dialpad;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

public class DialpadKeyButton extends FrameLayout {
    private static final int LONG_HOVER_TIMEOUT = (ViewConfiguration.getLongPressTimeout() * 2);
    private AccessibilityManager mAccessibilityManager;
    private CharSequence mBackupContentDesc;
    private RectF mHoverBounds = new RectF();
    private KeyTimes mKeyTimes;
    private CharSequence mLongHoverContentDesc;
    private Runnable mLongHoverRunnable;
    private boolean mLongHovered;
    private OnPressedListener mOnPressedListener;
    private boolean mWasClickable;
    private boolean mWasLongClickable;

    public interface KeyTimes {
        boolean isFirstPress();
    }

    public interface OnPressedListener {
        void onPressed(View view, boolean z);
    }

    public void setOnPressedListener(OnPressedListener onPressedListener) {
        this.mOnPressedListener = onPressedListener;
    }

    public void setKeyTimes(KeyTimes times) {
        this.mKeyTimes = times;
    }

    public DialpadKeyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initForAccessibility(context);
    }

    public DialpadKeyButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initForAccessibility(context);
    }

    private void initForAccessibility(Context context) {
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
    }

    public void setLongHoverContentDescription(CharSequence contentDescription) {
        this.mLongHoverContentDesc = contentDescription;
        if (this.mLongHovered) {
            super.setContentDescription(this.mLongHoverContentDesc);
        }
    }

    public void setContentDescription(CharSequence contentDescription) {
        if (this.mLongHovered) {
            this.mBackupContentDesc = contentDescription;
        } else {
            super.setContentDescription(contentDescription);
        }
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (this.mOnPressedListener != null) {
            this.mOnPressedListener.onPressed(this, pressed);
        }
    }

    public boolean isInScrollingContainer() {
        if (this.mKeyTimes == null || this.mKeyTimes.isFirstPress()) {
            return super.isInScrollingContainer();
        }
        return false;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mHoverBounds.left = (float) getPaddingLeft();
        this.mHoverBounds.right = (float) (w - getPaddingRight());
        this.mHoverBounds.top = (float) getPaddingTop();
        this.mHoverBounds.bottom = (float) (h - getPaddingBottom());
    }

    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action != 16) {
            return super.performAccessibilityAction(action, arguments);
        }
        simulateClickForAccessibility();
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (this.mAccessibilityManager.isEnabled() && this.mAccessibilityManager.isTouchExplorationEnabled()) {
            switch (event.getActionMasked()) {
                case 9:
                    this.mWasClickable = isClickable();
                    this.mWasLongClickable = isLongClickable();
                    if (this.mWasLongClickable && this.mLongHoverContentDesc != null) {
                        if (this.mLongHoverRunnable == null) {
                            this.mLongHoverRunnable = new Runnable() {
                                public void run() {
                                    DialpadKeyButton.this.setLongHovered(true);
                                    DialpadKeyButton.this.announceForAccessibility(DialpadKeyButton.this.mLongHoverContentDesc);
                                }
                            };
                        }
                        postDelayed(this.mLongHoverRunnable, (long) LONG_HOVER_TIMEOUT);
                    }
                    setClickable(false);
                    setLongClickable(false);
                    break;
                case 10:
                    if (this.mHoverBounds.contains(event.getX(), event.getY())) {
                        if (this.mLongHovered) {
                            performLongClick();
                        } else {
                            simulateClickForAccessibility();
                        }
                    }
                    cancelLongHover();
                    setClickable(this.mWasClickable);
                    setLongClickable(this.mWasLongClickable);
                    break;
            }
        }
        return super.onHoverEvent(event);
    }

    private void simulateClickForAccessibility() {
        if (!isPressed()) {
            setPressed(true);
            sendAccessibilityEvent(1);
            setPressed(false);
        }
    }

    private void setLongHovered(boolean enabled) {
        if (this.mLongHovered != enabled) {
            this.mLongHovered = enabled;
            if (enabled) {
                this.mBackupContentDesc = getContentDescription();
                super.setContentDescription(this.mLongHoverContentDesc);
                return;
            }
            super.setContentDescription(this.mBackupContentDesc);
        }
    }

    private void cancelLongHover() {
        if (this.mLongHoverRunnable != null) {
            removeCallbacks(this.mLongHoverRunnable);
        }
        setLongHovered(false);
    }
}
