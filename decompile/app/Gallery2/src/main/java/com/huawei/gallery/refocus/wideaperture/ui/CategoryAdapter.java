package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.huawei.gallery.refocus.wideaperture.app.WideApertureFilterAction;

public class CategoryAdapter extends ArrayAdapter<WideApertureFilterAction> {
    private View mContainer;
    private int mItemHeight;
    private int mItemWidth;
    private OnSelectedChangedListener mOnSelectedChangedListener;
    private int mOrientation;
    private int mSelectedPosition;

    public interface OnSelectedChangedListener {
        void onSelectedChanged(int i);
    }

    public CategoryAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mItemWidth = -1;
        this.mSelectedPosition = -1;
        this.mItemHeight = (int) (context.getResources().getDisplayMetrics().density * 100.0f);
    }

    public CategoryAdapter(Context context) {
        this(context, 0);
    }

    public void clear() {
        super.clear();
    }

    public void setItemHeight(int height) {
        this.mItemHeight = height;
    }

    public void setItemWidth(int width) {
        this.mItemWidth = width;
    }

    public void add(WideApertureFilterAction WideApertureFilterAction) {
        super.add(WideApertureFilterAction);
    }

    public void initializeSelection() {
        int selected = 0;
        for (int i = 0; i < getCount(); i++) {
            if (((WideApertureFilterAction) getItem(i)).isSelected()) {
                selected = i;
                break;
            }
        }
        setSelectedPosition(selected);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new CategoryView(getContext());
        }
        CategoryView view = (CategoryView) convertView;
        view.setOrientation(this.mOrientation);
        view.setAction((WideApertureFilterAction) getItem(position), this);
        view.setLayoutParams(new LayoutParams(this.mItemWidth, this.mItemHeight));
        view.setTag(Integer.valueOf(position));
        view.invalidate();
        return view;
    }

    public void setSelected(View v) {
        int old = this.mSelectedPosition;
        if (old != -1) {
            invalidateView(old);
        }
        ((WideApertureFilterAction) getItem(old)).setSelected(false);
        setSelectedPosition(((Integer) v.getTag()).intValue());
        ((WideApertureFilterAction) getItem(this.mSelectedPosition)).setSelected(true);
        invalidateView(this.mSelectedPosition);
    }

    public boolean isSelected(View v) {
        return ((Integer) v.getTag()).intValue() == this.mSelectedPosition;
    }

    private void invalidateView(int position) {
        View child;
        if (this.mContainer instanceof ListView) {
            ListView lv = this.mContainer;
            child = lv.getChildAt(position - lv.getFirstVisiblePosition());
        } else {
            child = this.mContainer.getChildAt(position);
        }
        if (child != null) {
            child.invalidate();
        }
    }

    public void setContainer(View container) {
        this.mContainer = container;
    }

    public void setSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void remove(WideApertureFilterAction WideApertureFilterAction) {
        super.remove(WideApertureFilterAction);
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    private void setSelectedPosition(int position) {
        if (this.mSelectedPosition != position) {
            this.mSelectedPosition = position;
            if (this.mOnSelectedChangedListener != null) {
                this.mOnSelectedChangedListener.onSelectedChanged(this.mSelectedPosition);
            }
        }
    }
}
