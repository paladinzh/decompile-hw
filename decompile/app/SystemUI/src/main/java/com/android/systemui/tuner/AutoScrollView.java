package com.android.systemui.tuner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.ScrollView;

public class AutoScrollView extends ScrollView {
    public AutoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case 2:
                int y = (int) event.getY();
                int height = getHeight();
                int scrollPadding = (int) (((float) height) * 0.1f);
                if (y >= scrollPadding) {
                    if (y > height - scrollPadding) {
                        scrollBy(0, (y - height) + scrollPadding);
                        break;
                    }
                }
                scrollBy(0, y - scrollPadding);
                break;
                break;
        }
        return false;
    }
}
