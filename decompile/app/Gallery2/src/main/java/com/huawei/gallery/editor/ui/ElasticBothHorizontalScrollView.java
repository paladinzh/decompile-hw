package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.gallery3d.common.Utils;

public class ElasticBothHorizontalScrollView extends ElasticHorizontalScrollView {
    public ElasticBothHorizontalScrollView(Context context) {
        super(context);
    }

    public ElasticBothHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaX = deltaX;
        if (isTouchEvent && scrollX < 0) {
            newDeltaX = Utils.getElasticInterpolation(deltaX, scrollX, 250);
        }
        int maxOverScrollXDistance = maxOverScrollX;
        if (newDeltaX + scrollX <= 0) {
            maxOverScrollXDistance = getWidth();
        }
        invalidate();
        return super.overScrollBy(newDeltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollXDistance, maxOverScrollY, isTouchEvent);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 1:
                if (getChildCount() <= 0 || getScrollX() >= 0) {
                    return super.onTouchEvent(ev);
                }
                ev.setAction(3);
                return super.onTouchEvent(ev);
            default:
                return super.onTouchEvent(ev);
        }
    }
}
