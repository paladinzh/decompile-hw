package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class MediaPickerGridView extends GridView {
    public MediaPickerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean canSwipeDown() {
        if (getAdapter() == null || getAdapter().getCount() == 0 || getChildCount() == 0) {
            return false;
        }
        if (getFirstVisiblePosition() != 0 || getChildAt(0).getTop() < 0) {
            return true;
        }
        return false;
    }
}
