package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class UnbounceListView extends ListView {
    public UnbounceListView(Context context) {
        super(context);
    }

    public UnbounceListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnbounceListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY + scrollY < 0) {
            deltaY = 0;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }
}
