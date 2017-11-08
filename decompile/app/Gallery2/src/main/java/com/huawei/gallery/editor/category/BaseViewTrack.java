package com.huawei.gallery.editor.category;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class BaseViewTrack extends LinearLayout {
    protected BaseViewAdapter mAdapter;
    protected DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onChanged() {
            super.onChanged();
            if (BaseViewTrack.this.mAdapter != null) {
                if (BaseViewTrack.this.isVolatile() || BaseViewTrack.this.getChildCount() != BaseViewTrack.this.mAdapter.getCount()) {
                    BaseViewTrack.this.fillContent();
                } else {
                    BaseViewTrack.this.invalidate();
                }
            }
        }

        public void onInvalidated() {
            super.onInvalidated();
            BaseViewTrack.this.fillContent();
        }
    };

    public BaseViewTrack(Context context) {
        super(context);
    }

    public BaseViewTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected boolean isVolatile() {
        return false;
    }

    public void setAdapter(BaseViewAdapter adapter) {
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        fillContent();
    }

    public void fillContent() {
        removeAllViews();
        if (this.mAdapter != null) {
            int n = this.mAdapter.getCount();
            for (int i = 0; i < n; i++) {
                addView(this.mAdapter.getView(i, null, this), i);
            }
            requestLayout();
        }
    }

    public void invalidate() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).invalidate();
        }
    }
}
