package com.android.contacts.compatibility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ExpandableListView;
import com.android.contacts.util.HwLog;
import java.lang.reflect.Field;

public class ExpandableListViewEx extends ExpandableListView {
    private static final Field GROUP_INDICATOR_FIELD = getField("mGroupIndicator");
    private static final Field INDICATOR_END_FIELD = getField("mIndicatorEnd");
    private static final Field INDICATOR_START_FIELD = getField("mIndicatorStart");
    private boolean mIsFirst;

    public ExpandableListViewEx(Context context) {
        super(context);
        this.mIsFirst = true;
    }

    public ExpandableListViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 16842863);
    }

    public ExpandableListViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsFirst = true;
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mIsFirst) {
            changeIndicatorPosition();
            this.mIsFirst = false;
        }
        super.dispatchDraw(canvas);
    }

    private void changeIndicatorPosition() {
        if (INDICATOR_START_FIELD != null && INDICATOR_END_FIELD != null) {
            try {
                int width = getWidth();
                int start = INDICATOR_START_FIELD.getInt(this);
                int end = INDICATOR_END_FIELD.getInt(this);
                if (end == 0) {
                    end = start + getIndictorWidth();
                }
                int temp = end;
                setIndicatorBoundsRelative(((width - temp) - getPaddingLeft()) - getPaddingRight(), ((width - start) - getPaddingLeft()) - getPaddingRight());
            } catch (IllegalAccessException e) {
                HwLog.w("ExpandableListViewEx", "illegal access exception.");
            }
        }
    }

    private int getIndictorWidth() throws IllegalArgumentException, IllegalAccessException {
        Drawable field = GROUP_INDICATOR_FIELD.get(this);
        if (field instanceof Drawable) {
            return field.getIntrinsicWidth();
        }
        return 0;
    }

    private static Field getField(String methodName) {
        Field field = null;
        try {
            field = ExpandableListView.class.getDeclaredField(methodName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            HwLog.w("ExpandableListViewEx", "method " + methodName + " not found");
            return field;
        }
    }
}
