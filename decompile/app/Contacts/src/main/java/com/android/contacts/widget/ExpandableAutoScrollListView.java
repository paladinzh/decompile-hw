package com.android.contacts.widget;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

public class ExpandableAutoScrollListView extends ExpandableListView {
    private int mRequestedScrollPosition = -1;
    private boolean mSmoothScrollRequested;

    public boolean performItemClick(View v, int position, long id) {
        super.performItemClick(v, position, id);
        setItemChecked(position, true);
        return true;
    }

    public ExpandableAutoScrollListView(Context context) {
        super(context);
    }

    public ExpandableAutoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableAutoScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void layoutChildren() {
        super.layoutChildren();
        if (this.mRequestedScrollPosition != -1) {
            int position = this.mRequestedScrollPosition;
            this.mRequestedScrollPosition = -1;
            int firstPosition = getFirstVisiblePosition() + 1;
            int lastPosition = getLastVisiblePosition();
            if (position < firstPosition || position > lastPosition) {
                int offset = Float.valueOf(((float) getHeight()) * 0.33f).intValue();
                if (this.mSmoothScrollRequested) {
                    int twoScreens = (lastPosition - firstPosition) * 2;
                    int preliminaryPosition;
                    if (position < firstPosition) {
                        preliminaryPosition = position + twoScreens;
                        if (preliminaryPosition >= getCount()) {
                            preliminaryPosition = getCount() - 1;
                        }
                        if (preliminaryPosition < firstPosition) {
                            setSelection(preliminaryPosition);
                            super.layoutChildren();
                        }
                    } else {
                        preliminaryPosition = position - twoScreens;
                        if (preliminaryPosition < 0) {
                            preliminaryPosition = 0;
                        }
                        if (preliminaryPosition > lastPosition) {
                            setSelection(preliminaryPosition);
                            super.layoutChildren();
                        }
                    }
                    smoothScrollToPositionFromTop(position, offset);
                } else {
                    setSelectionFromTop(position, offset);
                    super.layoutChildren();
                }
            }
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        ListAdapter adapter = getAdapter();
        if (adapter != null && (adapter instanceof BaseAdapter)) {
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }
}
