package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.tools.LanguageUtils;

public class TimeListView extends ListView {
    private static final String TAG = "TimeListView";
    private boolean isRtl;
    private Drawable mDrawable;
    private int mLineWidth;
    private int mTimeWidth;

    public TimeListView(Context context) {
        super(context);
        init();
    }

    public TimeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.mTimeWidth = (int) getContext().getResources().getDimension(R.dimen.space_cleaner_time_width);
        this.mDrawable = getContext().getResources().getDrawable(R.drawable.list_time);
        this.mLineWidth = this.mDrawable.getIntrinsicWidth();
        this.isRtl = LanguageUtils.isRTL();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (getCount() == getChildCount() && getCount() > 0) {
            int start;
            int end;
            if (this.isRtl) {
                start = (getWidth() - this.mTimeWidth) - this.mLineWidth;
                end = getWidth() - this.mTimeWidth;
            } else {
                start = this.mTimeWidth;
                end = start + this.mLineWidth;
            }
            this.mDrawable.setBounds(start, getTotalHeightofListView(), end, getHeight());
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getCount() == getChildCount() && getCount() > 0 && this.mDrawable != null) {
            this.mDrawable.draw(canvas);
        }
    }

    public int getTotalHeightofListView() {
        ListAdapter mAdapter = getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, this);
            if (mView != null) {
                mView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
                totalHeight += mView.getMeasuredHeight();
            }
        }
        return totalHeight;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TAG);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TAG);
    }
}
