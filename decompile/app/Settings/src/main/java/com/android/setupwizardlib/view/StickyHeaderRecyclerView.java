package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

public class StickyHeaderRecyclerView extends HeaderRecyclerView {
    private int mStatusBarInset = 0;
    private View mSticky;
    private RectF mStickyRect = new RectF();

    public StickyHeaderRecyclerView(Context context) {
        super(context);
    }

    public StickyHeaderRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyHeaderRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mSticky == null) {
            updateStickyView();
        }
        if (this.mSticky != null) {
            View headerView = getHeader();
            if (headerView != null && headerView.getHeight() == 0) {
                headerView.layout(0, -headerView.getMeasuredHeight(), headerView.getMeasuredWidth(), 0);
            }
        }
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (this.mSticky != null) {
            measureChild(getHeader(), widthSpec, heightSpec);
        }
    }

    public void updateStickyView() {
        View header = getHeader();
        if (header != null) {
            this.mSticky = header.findViewWithTag("sticky");
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mSticky != null) {
            View headerView = getHeader();
            int saveCount = canvas.save();
            View drawTarget = headerView != null ? headerView : this.mSticky;
            int drawOffset = headerView != null ? this.mSticky.getTop() : 0;
            if (drawTarget.getTop() + drawOffset < this.mStatusBarInset || !drawTarget.isShown()) {
                this.mStickyRect.set(0.0f, (float) ((-drawOffset) + this.mStatusBarInset), (float) drawTarget.getWidth(), (float) ((drawTarget.getHeight() - drawOffset) + this.mStatusBarInset));
                canvas.translate(0.0f, this.mStickyRect.top);
                canvas.clipRect(0, 0, drawTarget.getWidth(), drawTarget.getHeight());
                drawTarget.draw(canvas);
            } else {
                this.mStickyRect.setEmpty();
            }
            canvas.restoreToCount(saveCount);
        }
    }

    @TargetApi(21)
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (getFitsSystemWindows()) {
            this.mStatusBarInset = insets.getSystemWindowInsetTop();
            insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
        return insets;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!this.mStickyRect.contains(ev.getX(), ev.getY())) {
            return super.dispatchTouchEvent(ev);
        }
        ev.offsetLocation(-this.mStickyRect.left, -this.mStickyRect.top);
        return getHeader().dispatchTouchEvent(ev);
    }
}
