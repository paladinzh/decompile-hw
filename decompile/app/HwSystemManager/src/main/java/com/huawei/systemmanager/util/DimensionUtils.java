package com.huawei.systemmanager.util;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;

public class DimensionUtils {
    public static int getAreaOne(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }

    public static int getAreaThree(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().findViewById(16908290).getDrawingRect(rect);
        return rect.height();
    }
}
