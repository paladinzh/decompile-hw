package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.huawei.watermark.manager.parse.WMElement;

public class SelectOrPressColorfulImageView extends SelectColorfulImageView {
    public SelectOrPressColorfulImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
            case 2:
                setAlpha(0.5f);
                break;
            case 1:
                setAlpha(WMElement.CAMERASIZEVALUE1B1);
                break;
            default:
                setAlpha(WMElement.CAMERASIZEVALUE1B1);
                break;
        }
        return super.onTouchEvent(event);
    }
}
