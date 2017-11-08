package com.huawei.systemmanager.comm.widget.StickyListView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

class WrapperView extends ViewGroup {
    Drawable mDivider;
    int mDividerHeight;
    View mHeader;
    View mItem;
    int mItemTop;

    public WrapperView(Context c) {
        super(c);
    }

    void update(View item, View header, Drawable divider, int dividerHeight) {
        if (item == null) {
            throw new NullPointerException("List view item must not be null.");
        }
        if (this.mItem != item) {
            removeView(this.mItem);
            this.mItem = item;
            Object parent = item.getParent();
            if (!(parent == null || parent == this || !(parent instanceof ViewGroup))) {
                ((ViewGroup) parent).removeView(item);
            }
            addView(item);
        }
        if (this.mHeader != header) {
            if (this.mHeader != null) {
                removeView(this.mHeader);
            }
            this.mHeader = header;
            if (header != null) {
                addView(header);
            }
        }
        if (this.mDivider != divider) {
            this.mDivider = divider;
            this.mDividerHeight = dividerHeight;
            invalidate();
        }
    }

    boolean hasHeader() {
        return this.mHeader != null;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams params;
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, 1073741824);
        int measuredHeight = 0;
        if (this.mHeader != null) {
            params = this.mHeader.getLayoutParams();
            if (params == null || params.height <= 0) {
                this.mHeader.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, 0));
            } else {
                this.mHeader.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(params.height, 1073741824));
            }
            measuredHeight = this.mHeader.getMeasuredHeight() + 0;
        } else if (this.mDivider != null) {
            measuredHeight = this.mDividerHeight + 0;
        }
        params = this.mItem.getLayoutParams();
        if (params == null || params.height <= 0) {
            this.mItem.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, 0));
        } else {
            this.mItem.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(params.height, 1073741824));
        }
        setMeasuredDimension(measuredWidth, measuredHeight + this.mItem.getMeasuredHeight());
    }

    protected void onLayout(boolean changed, int ll, int tt, int rr, int bb) {
        int r = getWidth();
        int b = getHeight();
        if (this.mHeader != null) {
            int headerHeight = this.mHeader.getMeasuredHeight();
            this.mHeader.layout(0, 0, r, headerHeight);
            this.mItemTop = headerHeight;
            this.mItem.layout(0, headerHeight, r, b);
        } else if (this.mDivider != null) {
            this.mDivider.setBounds(0, 0, r, this.mDividerHeight);
            this.mItemTop = this.mDividerHeight;
            this.mItem.layout(0, this.mDividerHeight, r, b);
        } else {
            this.mItemTop = 0;
            this.mItem.layout(0, 0, r, b);
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mHeader == null && this.mDivider != null) {
            if (VERSION.SDK_INT < 11) {
                canvas.clipRect(0, 0, getWidth(), this.mDividerHeight);
            }
            this.mDivider.draw(canvas);
        }
    }
}
