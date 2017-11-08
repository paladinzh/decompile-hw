package com.android.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.widget.ViewFlipper;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.keyguard.util.HwLog;

public class KeyguardSecurityViewFlipper extends ViewFlipper implements KeyguardSecurityView {
    private Rect mTempRect;

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        @ExportedProperty(category = "layout")
        public int maxHeight;
        @ExportedProperty(category = "layout")
        public int maxWidth;

        public LayoutParams(android.view.ViewGroup.LayoutParams other) {
            super(other);
        }

        public LayoutParams(LayoutParams other) {
            super(other);
            this.maxWidth = other.maxWidth;
            this.maxHeight = other.maxHeight;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R$styleable.KeyguardSecurityViewFlipper_Layout, 0, 0);
            this.maxWidth = a.getDimensionPixelSize(R$styleable.KeyguardSecurityViewFlipper_Layout_layout_maxWidth, 0);
            this.maxHeight = a.getDimensionPixelSize(R$styleable.KeyguardSecurityViewFlipper_Layout_layout_maxHeight, 0);
            a.recycle();
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:maxWidth", this.maxWidth);
            encoder.addProperty("layout:maxHeight", this.maxHeight);
        }
    }

    public KeyguardSecurityViewFlipper(Context context) {
        this(context, null);
    }

    public KeyguardSecurityViewFlipper(Context context, AttributeSet attr) {
        super(context, attr);
        this.mTempRect = new Rect();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        this.mTempRect.set(0, 0, 0, 0);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                offsetRectIntoDescendantCoords(child, this.mTempRect);
                ev.offsetLocation((float) this.mTempRect.left, (float) this.mTempRect.top);
                if (child.dispatchTouchEvent(ev)) {
                    result = true;
                }
                ev.offsetLocation((float) (-this.mTempRect.left), (float) (-this.mTempRect.top));
            }
        }
        return result;
    }

    KeyguardSecurityView getSecurityView() {
        View child = getChildAt(getDisplayedChild());
        if (child instanceof KeyguardSecurityView) {
            return (KeyguardSecurityView) child;
        }
        return null;
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.setKeyguardCallback(callback);
        }
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.setLockPatternUtils(utils);
        }
    }

    public void onPause() {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.onPause();
        }
    }

    public void onResume(int reason) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.onResume(reason);
        }
    }

    public boolean needsInput() {
        KeyguardSecurityView ksv = getSecurityView();
        return ksv != null ? ksv.needsInput() : false;
    }

    public void showPromptReason(int reason) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.showPromptReason(reason);
        }
    }

    public void showMessage(String message, int color) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.showMessage(message, color);
        }
    }

    public void startAppearAnimation() {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            ksv.startAppearAnimation();
        }
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            return ksv.startDisappearAnimation(finishRunnable);
        }
        return false;
    }

    public boolean startRevertAnimation(Runnable finishRunnable) {
        KeyguardSecurityView ksv = getSecurityView();
        if (ksv != null) {
            return ksv.startRevertAnimation(finishRunnable);
        }
        return false;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? new LayoutParams((LayoutParams) p) : new LayoutParams(p);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        int i;
        int widthMode = MeasureSpec.getMode(widthSpec);
        int heightMode = MeasureSpec.getMode(heightSpec);
        if (widthMode != Integer.MIN_VALUE) {
            HwLog.w("KeyguardSecurityViewFlipper", "onMeasure: widthSpec " + MeasureSpec.toString(widthSpec) + " should be AT_MOST");
        }
        if (heightMode != Integer.MIN_VALUE) {
            HwLog.w("KeyguardSecurityViewFlipper", "onMeasure: heightSpec " + MeasureSpec.toString(heightSpec) + " should be AT_MOST");
        }
        int widthSize = MeasureSpec.getSize(widthSpec);
        int heightSize = MeasureSpec.getSize(heightSpec);
        int maxWidth = widthSize;
        int maxHeight = heightSize;
        int count = getChildCount();
        for (i = 0; i < count; i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.maxWidth > 0 && lp.maxWidth < maxWidth) {
                maxWidth = lp.maxWidth;
            }
            if (lp.maxHeight > 0 && lp.maxHeight < maxHeight) {
                maxHeight = lp.maxHeight;
            }
        }
        int wPadding = getPaddingLeft() + getPaddingRight();
        int hPadding = getPaddingTop() + getPaddingBottom();
        maxWidth = Math.max(0, maxWidth - wPadding);
        maxHeight = Math.max(0, maxHeight - hPadding);
        int width = widthMode == 1073741824 ? widthSize : 0;
        int height = heightMode == 1073741824 ? heightSize : 0;
        for (i = 0; i < count; i++) {
            View child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            child.measure(makeChildMeasureSpec(maxWidth, lp.width), makeChildMeasureSpec(maxHeight, lp.height));
            width = Math.max(width, Math.min(child.getMeasuredWidth(), widthSize - wPadding));
            height = Math.max(height, Math.min(child.getMeasuredHeight(), heightSize - hPadding));
        }
        setMeasuredDimension(width + wPadding, height + hPadding);
    }

    private int makeChildMeasureSpec(int maxSize, int childDimen) {
        int mode;
        int size;
        switch (childDimen) {
            case -2:
                mode = Integer.MIN_VALUE;
                size = maxSize;
                break;
            case -1:
                mode = 1073741824;
                size = maxSize;
                break;
            default:
                mode = 1073741824;
                size = Math.min(maxSize, childDimen);
                break;
        }
        return MeasureSpec.makeMeasureSpec(size, mode);
    }
}
