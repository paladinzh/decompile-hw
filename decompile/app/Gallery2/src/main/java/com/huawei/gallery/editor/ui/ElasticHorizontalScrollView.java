package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import com.android.gallery3d.common.Utils;

public class ElasticHorizontalScrollView extends HorizontalScrollView {
    public ElasticHorizontalScrollView(Context context) {
        super(context);
    }

    public ElasticHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElasticHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 1:
                if (getChildCount() <= 0 || getScrollX() < Math.max(0, getChildAt(0).getWidth() - ((getWidth() - getLeftPaddingOffset()) - getRightPaddingOffset()))) {
                    return super.onTouchEvent(ev);
                }
                ev.setAction(3);
                return super.onTouchEvent(ev);
            default:
                return super.onTouchEvent(ev);
        }
    }

    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaX = deltaX;
        if (isTouchEvent && scrollRangeX <= scrollX) {
            newDeltaX = Utils.getElasticInterpolation(deltaX, scrollX - scrollRangeX, 250);
        }
        int maxOverScrollXDistance = maxOverScrollX;
        if (newDeltaX + scrollX >= scrollRangeX) {
            maxOverScrollXDistance = getWidth();
        }
        invalidate();
        return super.overScrollBy(newDeltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollXDistance, maxOverScrollY, isTouchEvent);
    }
}
