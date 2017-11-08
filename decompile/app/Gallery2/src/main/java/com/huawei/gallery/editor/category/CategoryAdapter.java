package com.huawei.gallery.editor.category;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class CategoryAdapter extends BaseViewAdapter {
    protected int mItemHeight;
    protected int mItemWidth;
    protected int mOrientation;

    public CategoryAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mItemWidth = -1;
        this.mItemHeight = (int) (context.getResources().getDisplayMetrics().density * 100.0f);
    }

    public CategoryAdapter(Context context) {
        this(context, 0);
    }

    public CategoryAdapter(Context context, BaseViewAdapter baseViewAdapter) {
        super(context, baseViewAdapter);
        this.mItemWidth = -1;
    }

    public void setItemHeight(int height) {
        this.mItemHeight = height;
    }

    public void setItemWidth(int width) {
        this.mItemWidth = width;
    }

    public void initializeSelection() {
        setSelectedPosition(0);
        invalidateView(0);
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public CategoryView findViewByAction(Action action) {
        if (this.mContainer == null || !(this.mContainer instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = this.mContainer;
        for (int index = 0; index < group.getChildCount(); index++) {
            View view = group.getChildAt(index);
            if (action == getItem(((Integer) view.getTag()).intValue())) {
                if (view instanceof CategoryView) {
                    return (CategoryView) view;
                }
                return null;
            }
        }
        return null;
    }

    public void onClickSelectView() {
        if (this.mSelectedPosition != -1) {
            CategoryView view = findViewByAction((Action) getItem(this.mSelectedPosition));
            if (view != null) {
                view.onClick(view);
            }
        }
    }
}
