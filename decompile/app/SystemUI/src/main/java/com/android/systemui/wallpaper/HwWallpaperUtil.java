package com.android.systemui.wallpaper;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import com.android.systemui.utils.PerfAdjust;

public class HwWallpaperUtil {
    public static boolean isPerformancePreferred() {
        if (PerfAdjust.isEmuiLite()) {
            return true;
        }
        return false;
    }

    public static Point getPoint(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public static boolean isFixedScreen(Context context, int bmW, int bmH) {
        Point size = getPoint(context);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        if ((bmW == max && bmH == max) || ((bmW == max && bmH == min) || (bmW == min && bmH == max))) {
            return true;
        }
        return false;
    }
}
