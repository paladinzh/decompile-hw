package com.android.systemui.qs;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import com.android.systemui.R$styleable;
import com.android.systemui.utils.HwLog;

public class AutoSizingList extends LinearLayout {
    private ListAdapter mAdapter;
    private final Runnable mBindChildren = new Runnable() {
        public void run() {
            AutoSizingList.this.rebindChildren();
        }
    };
    private int mCount;
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        public void onChanged() {
            HwLog.i("AutoSizingList", " onChanged");
            if (AutoSizingList.this.mCount > AutoSizingList.this.getDesiredCount()) {
                AutoSizingList.this.mCount = AutoSizingList.this.getDesiredCount();
            }
            AutoSizingList.this.postRebindChildren();
        }

        public void onInvalidated() {
            AutoSizingList.this.postRebindChildren();
        }
    };
    private final Handler mHandler = new Handler();
    private final int mItemSize;

    public AutoSizingList(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mItemSize = context.obtainStyledAttributes(attrs, R$styleable.AutoSizingList).getDimensionPixelSize(0, 0);
    }

    public void setAdapter(ListAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerDataSetObserver(this.mDataObserver);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int requestedHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (requestedHeight != 0) {
            int count = Math.min(requestedHeight / this.mItemSize, getDesiredCount());
            if (this.mCount != count) {
                postRebindChildren();
                this.mCount = count;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getDesiredCount() {
        return this.mAdapter != null ? this.mAdapter.getCount() : 0;
    }

    private void postRebindChildren() {
        this.mHandler.post(this.mBindChildren);
    }

    private void rebindChildren() {
        if (this.mAdapter != null) {
            int i = 0;
            while (i < this.mCount) {
                View childAt = i < getChildCount() ? getChildAt(i) : null;
                View newView = this.mAdapter.getView(i, childAt, this);
                if (newView != childAt) {
                    if (childAt != null) {
                        removeView(childAt);
                    }
                    addView(newView, i);
                }
                i++;
            }
            while (getChildCount() > this.mCount) {
                removeViewAt(getChildCount() - 1);
            }
        }
    }
}
