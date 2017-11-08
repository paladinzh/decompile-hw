package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.gallery3d.R$styleable;

public class CategoryTrack extends LinearLayout {
    private CategoryAdapter mAdapter;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onChanged() {
            super.onChanged();
            if (CategoryTrack.this.getChildCount() != CategoryTrack.this.mAdapter.getCount()) {
                CategoryTrack.this.fillContent();
            } else {
                CategoryTrack.this.invalidate();
            }
        }

        public void onInvalidated() {
            super.onInvalidated();
            CategoryTrack.this.fillContent();
        }
    };
    private int mElemHeight;
    private int mElemWidth;
    private boolean mTextDrawOnView = true;

    public CategoryTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.CategoryTrack);
        this.mElemWidth = a.getDimensionPixelSize(0, 0);
        this.mElemHeight = a.getDimensionPixelSize(1, 0);
        if (this.mTextDrawOnView) {
            this.mElemHeight = a.getDimensionPixelSize(0, 0);
        } else {
            this.mElemHeight = a.getDimensionPixelSize(1, 0);
        }
        a.recycle();
    }

    public void setAdapter(CategoryAdapter adapter) {
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        fillContent();
    }

    public void fillContent() {
        removeAllViews();
        this.mAdapter.setItemWidth(this.mElemWidth);
        this.mAdapter.setItemHeight(this.mElemHeight);
        int n = this.mAdapter.getCount();
        for (int i = 0; i < n; i++) {
            addView(this.mAdapter.getView(i, null, this), i);
        }
        requestLayout();
    }

    public void setAllViewsClickable(boolean clickable) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setClickable(clickable);
        }
    }

    public void invalidate() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).invalidate();
        }
    }
}
