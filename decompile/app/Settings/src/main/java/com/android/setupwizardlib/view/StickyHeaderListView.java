package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ListView;
import com.android.setupwizardlib.R$styleable;

public class StickyHeaderListView extends ListView {
    private int mStatusBarInset = 0;
    private View mSticky;
    private View mStickyContainer;
    private RectF mStickyRect = new RectF();

    public StickyHeaderListView(Context context) {
        super(context);
        init(null, 16842868);
    }

    public StickyHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 16842868);
    }

    public StickyHeaderListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.SuwStickyHeaderListView, defStyleAttr, 0);
        int headerResId = a.getResourceId(R$styleable.SuwStickyHeaderListView_suwHeader, 0);
        if (headerResId != 0) {
            addHeaderView(LayoutInflater.from(getContext()).inflate(headerResId, this, false), null, false);
        }
        a.recycle();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mSticky == null) {
            updateStickyView();
        }
    }

    public void updateStickyView() {
        this.mSticky = findViewWithTag("sticky");
        this.mStickyContainer = findViewWithTag("stickyContainer");
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!this.mStickyRect.contains(ev.getX(), ev.getY())) {
            return super.dispatchTouchEvent(ev);
        }
        ev.offsetLocation(-this.mStickyRect.left, -this.mStickyRect.top);
        return this.mStickyContainer.dispatchTouchEvent(ev);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mSticky != null) {
            int saveCount = canvas.save();
            View drawTarget = this.mStickyContainer != null ? this.mStickyContainer : this.mSticky;
            int drawOffset = this.mStickyContainer != null ? this.mSticky.getTop() : 0;
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

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        int numberOfHeaders = this.mSticky != null ? 1 : 0;
        event.setItemCount(event.getItemCount() - numberOfHeaders);
        event.setFromIndex(Math.max(event.getFromIndex() - numberOfHeaders, 0));
        if (VERSION.SDK_INT >= 14) {
            event.setToIndex(Math.max(event.getToIndex() - numberOfHeaders, 0));
        }
    }
}
