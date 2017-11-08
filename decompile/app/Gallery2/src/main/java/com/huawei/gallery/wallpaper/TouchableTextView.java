package com.huawei.gallery.wallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class TouchableTextView extends TextView {
    public TouchableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        setAlpha(isPressed() ? 0.5f : WMElement.CAMERASIZEVALUE1B1);
        if (isSelected()) {
            ColorfulUtils.decorateColorfulForTextView(getContext(), this);
        }
        super.onDraw(canvas);
    }
}
