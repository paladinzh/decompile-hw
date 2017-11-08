package com.android.server.rog;

import android.content.Context;
import android.graphics.Rect;
import android.rog.AppRogInfo;
import android.util.Slog;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.input.HwCircleAnimation;
import java.util.HashMap;
import java.util.Map;

public class HwRogPolicy implements IRogPolicy {
    private static final String TAG = "HwRogPolicy";
    private static Map<Integer, Rect> sResolutionLevelToRectMap = new HashMap();
    private float mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
    private Context mContext;

    static {
        sResolutionLevelToRectMap.put(Integer.valueOf(1), new Rect(0, 0, 2160, 3840));
        sResolutionLevelToRectMap.put(Integer.valueOf(2), new Rect(0, 0, HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE, 2560));
        sResolutionLevelToRectMap.put(Integer.valueOf(3), new Rect(0, 0, 1080, 1920));
        sResolutionLevelToRectMap.put(Integer.valueOf(4), new Rect(0, 0, 720, 1280));
    }

    public HwRogPolicy(Context context) {
        this.mContext = context;
    }

    public AppRogInfo getAppOwnInfo(HwRogInfosCollector dataCollector, String packageName) {
        if (dataCollector.isInList(packageName)) {
            return dataCollector.getAppRogInfo(packageName);
        }
        return generateDefaultInfo(packageName);
    }

    private AppRogInfo generateDefaultInfo(String packageName) {
        return null;
    }

    private Rect resolutionLevelToPixel(int level) {
        if (!sResolutionLevelToRectMap.containsKey(Integer.valueOf(level))) {
            level = 3;
        }
        return (Rect) sResolutionLevelToRectMap.get(Integer.valueOf(level));
    }

    private int pixelToResolutionLevel(int widthPixel, int heightPixel) {
        if (widthPixel < 720) {
            return -1;
        }
        if (widthPixel < 1080) {
            return 4;
        }
        if (widthPixel < HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE) {
            return 3;
        }
        if (widthPixel < 2160) {
            return 2;
        }
        return 1;
    }

    public void calRogAppScale() {
        int screenWidth = this.mContext.getResources().getDisplayMetrics().noncompatWidthPixels;
        int deviceResolutionLevel = pixelToResolutionLevel(screenWidth, this.mContext.getResources().getDisplayMetrics().noncompatHeightPixels);
        if (deviceResolutionLevel < 0) {
            Slog.i(TAG, "calRogAppScale->invalid resolution level, set to no scale");
            this.mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
            return;
        }
        this.mAppRogScale = (((float) screenWidth) * HwCircleAnimation.SMALL_ALPHA) / ((float) resolutionLevelToPixel(deviceResolutionLevel + 1).width());
        if (Float.compare(this.mAppRogScale, HwCircleAnimation.SMALL_ALPHA) < 0) {
            Slog.i(TAG, "calRogAppScale->invalid scale factor, set to no scale");
            this.mAppRogScale = HwCircleAnimation.SMALL_ALPHA;
        }
        Slog.i(TAG, "calRogAppScale->rog sclae:" + this.mAppRogScale);
    }

    public float getAppRogScale() {
        return this.mAppRogScale;
    }
}
