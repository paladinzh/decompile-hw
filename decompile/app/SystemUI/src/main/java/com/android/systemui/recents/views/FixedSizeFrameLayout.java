package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;

public class FixedSizeFrameLayout extends FrameLayout {
    private final Rect mLayoutBounds = new Rect();

    public FixedSizeFrameLayout(Context context) {
        super(context);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureContents(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    protected final void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mLayoutBounds.set(left, top, right, bottom);
        layoutContents(this.mLayoutBounds, changed);
    }

    public final void requestLayout() {
        if (this.mLayoutBounds == null || this.mLayoutBounds.isEmpty()) {
            super.requestLayout();
            return;
        }
        measureContents(getMeasuredWidth(), getMeasuredHeight());
        layoutContents(this.mLayoutBounds, false);
    }

    protected void measureContents(int width, int height) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
    }

    protected void layoutContents(Rect bounds, boolean changed) {
        super.onLayout(changed, bounds.left, bounds.top, bounds.right, bounds.bottom);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        onSizeChanged(width, height, width, height);
    }
}
