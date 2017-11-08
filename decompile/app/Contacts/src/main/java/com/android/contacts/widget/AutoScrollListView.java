package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class AutoScrollListView extends ListView {
    private int mRequestedScrollPosition = -1;
    private boolean mSmoothScrollRequested;

    public AutoScrollListView(Context context) {
        super(context);
    }

    public AutoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void requestPositionToScreen(int position, boolean smoothScroll) {
        this.mRequestedScrollPosition = position;
        this.mSmoothScrollRequested = smoothScroll;
        requestLayout();
    }

    protected void layoutChildren() {
        super.layoutChildren();
        if (this.mRequestedScrollPosition != -1) {
            int position = this.mRequestedScrollPosition;
            this.mRequestedScrollPosition = -1;
            int firstPosition = getFirstVisiblePosition() + 1;
            int lastPosition = getLastVisiblePosition() - 1;
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
}
