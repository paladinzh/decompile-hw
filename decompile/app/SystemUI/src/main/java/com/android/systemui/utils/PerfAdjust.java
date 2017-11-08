package com.android.systemui.utils;

import android.os.SystemProperties;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.recents.views.TaskView;

public final class PerfAdjust {
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_NOVA_PERFORMANCE = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final boolean SUPPORT_ROTATION = SystemProperties.getBoolean("lockscreen.rot_override", false);

    public static long adjustPeekRunnableTimeout(long oriTimeout) {
        return Math.min(16, oriTimeout);
    }

    public static int getEnterFromHomeTranslationDuration(int defaultDuration) {
        int i;
        if (HwRecentsHelper.IS_EMUI_LITE) {
            i = 150;
        } else {
            i = 200;
        }
        return Math.min(defaultDuration, i);
    }

    public static int getTasksRemoveAllAnimationDuration(int defaultDuration) {
        return Math.min(defaultDuration, 100);
    }

    public static int getFrameOffsetMs(int defaultFrameOffSet) {
        return Math.min(defaultFrameOffSet, 16);
    }

    public static long getSwipeDeleteOneAnimationDuration(View view, long defaultDuration) {
        return (HwRecentsHelper.IS_EMUI_LITE && (view instanceof TaskView)) ? defaultDuration / 2 : defaultDuration;
    }

    public static float getSwipeDeleteOneAnimationVelocity(View view, float defaultVelocity) {
        return (HwRecentsHelper.IS_EMUI_LITE && (view instanceof TaskView)) ? defaultVelocity * 2.0f : defaultVelocity;
    }

    public static int getScrollTaskViewAnimationDuration(int defaultDuration) {
        return HwRecentsHelper.IS_EMUI_LITE ? defaultDuration / 2 : defaultDuration;
    }

    public static Interpolator getScrollTaskViewAnimationInterpolator() {
        if (HwRecentsHelper.IS_EMUI_LITE) {
            return Interpolators.LINEAR_OUT_SLOW_IN_LITE;
        }
        return Interpolators.FAST_OUT_SLOW_IN;
    }

    public static int getQuickSettingsTilesDefault() {
        if (HwRecentsHelper.IS_EMUI_LITE) {
            return R.string.quick_settings_tiles_default_lite;
        }
        return R.string.quick_settings_tiles_default;
    }

    public static int getQuickSettingsNumColumns() {
        if (HwRecentsHelper.IS_EMUI_LITE) {
            return R.integer.hw_quick_settings_num_columns_lite;
        }
        return R.integer.hw_quick_settings_num_columns;
    }

    public static int getQuickSettingsNumRows() {
        if (HwRecentsHelper.IS_EMUI_LITE) {
            return R.integer.hw_quick_settings_num_rows_lite;
        }
        return R.integer.hw_quick_settings_num_rows;
    }

    public static boolean supportBlurBackgound() {
        return !HwRecentsHelper.IS_EMUI_LITE;
    }

    public static float getDefaultStartPeekHeightLight() {
        return HwRecentsHelper.IS_EMUI_LITE ? 24.0f : 12.0f;
    }

    public static boolean supportScreenRotation() {
        return SUPPORT_ROTATION;
    }

    public static boolean isEmuiLite() {
        return !IS_EMUI_LITE ? IS_NOVA_PERFORMANCE : true;
    }
}
