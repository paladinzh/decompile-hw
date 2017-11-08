package com.android.settingslib.display;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.view.WindowManagerGlobal;
import com.android.settingslib.R$string;
import java.util.Arrays;

public class DisplayDensityUtils {
    private static final int[] SUMMARIES_LARGER = new int[]{R$string.screen_zoom_summary_large, R$string.screen_zoom_summary_very_large, R$string.screen_zoom_summary_extremely_large};
    private static final int[] SUMMARIES_SMALLER = new int[]{R$string.screen_zoom_summary_small};
    private static final int SUMMARY_CUSTOM = R$string.screen_zoom_summary_custom;
    public static final int SUMMARY_DEFAULT = R$string.screen_zoom_summary_default;
    private final int mCurrentIndex;
    private final int mDefaultDensity;
    private final String[] mEntries;
    private final int[] mValues;

    private int getDensityFromProp(int defaultDensity) {
        int density = SystemProperties.getInt("ro.sf.lcd_density", defaultDensity);
        Log.i("DisplayDensityUtils", "DisplayDensityUtils-->getDensityFromProp()-->density = " + density);
        return density;
    }

    public DisplayDensityUtils(Context context) {
        int defaultDensity = getDefaultDisplayDensity(0);
        int initDpi = 0;
        try {
            initDpi = Secure.getInt(context.getContentResolver(), "init_dpi");
        } catch (Exception exc) {
            Log.e("DisplayDensityUtils", "DisplayDensityUtils-->exc : " + exc);
        }
        if (initDpi > 0) {
            defaultDensity = initDpi;
        }
        defaultDensity = getDensityFromProp(defaultDensity);
        if (defaultDensity <= 0) {
            this.mEntries = null;
            this.mValues = null;
            this.mDefaultDensity = 0;
            this.mCurrentIndex = -1;
            return;
        }
        float interval;
        int i;
        int density;
        int displayIndex;
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int currentDensity = metrics.densityDpi;
        int currentDensityIndex = -1;
        float maxScale = Math.min(1.5f, ((float) ((Math.min(metrics.widthPixels, metrics.heightPixels) * 160) / 320)) / ((float) defaultDensity));
        int numLarger = (int) MathUtils.constrain((maxScale - 1.0f) / 0.09f, 0.0f, (float) SUMMARIES_LARGER.length);
        int numSmaller = (int) MathUtils.constrain(1.6666664f, 0.0f, (float) SUMMARIES_SMALLER.length);
        String[] entries = new String[((numSmaller + 1) + numLarger)];
        int[] values = new int[entries.length];
        int curIndex = 0;
        if (numSmaller > 0) {
            interval = 0.14999998f / ((float) numSmaller);
            for (i = numSmaller - 1; i >= 0; i--) {
                density = ((int) (((float) defaultDensity) * (1.0f - (((float) (i + 1)) * interval)))) & -2;
                if (currentDensity == density) {
                    currentDensityIndex = curIndex;
                }
                entries[curIndex] = res.getString(SUMMARIES_SMALLER[i]);
                values[curIndex] = density;
                curIndex++;
            }
        }
        if (currentDensity == defaultDensity) {
            currentDensityIndex = curIndex;
        }
        values[curIndex] = defaultDensity;
        entries[curIndex] = res.getString(SUMMARY_DEFAULT);
        curIndex++;
        if (numLarger > 0) {
            interval = (maxScale - 1.0f) / ((float) numLarger);
            for (i = 0; i < numLarger; i++) {
                density = ((int) (((float) defaultDensity) * ((((float) (i + 1)) * interval) + 1.0f))) & -2;
                if (currentDensity == density) {
                    currentDensityIndex = curIndex;
                }
                values[curIndex] = density;
                entries[curIndex] = res.getString(SUMMARIES_LARGER[i]);
                curIndex++;
            }
        }
        if (currentDensityIndex >= 0) {
            displayIndex = currentDensityIndex;
        } else {
            int newLength = values.length + 1;
            values = Arrays.copyOf(values, newLength);
            values[curIndex] = currentDensity;
            entries = (String[]) Arrays.copyOf(entries, newLength);
            entries[curIndex] = res.getString(SUMMARY_CUSTOM, new Object[]{Integer.valueOf(currentDensity)});
            displayIndex = curIndex;
        }
        this.mDefaultDensity = defaultDensity;
        this.mCurrentIndex = displayIndex;
        this.mEntries = entries;
        this.mValues = values;
    }

    public String[] getEntries() {
        return this.mEntries;
    }

    public int[] getValues() {
        return this.mValues;
    }

    public int getCurrentIndex() {
        return this.mCurrentIndex;
    }

    public int getDefaultDensity() {
        return this.mDefaultDensity;
    }

    public static int getDefaultDisplayDensity(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getInitialDisplayDensity(displayId);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static void clearForcedDisplayDensity(final int displayId) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    WindowManagerGlobal.getWindowManagerService().clearForcedDisplayDensity(displayId);
                } catch (RemoteException e) {
                    Log.w("DisplayDensityUtils", "Unable to clear forced display density setting");
                }
            }
        });
    }

    public static void setForcedDisplayDensity(final int displayId, final int density) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    WindowManagerGlobal.getWindowManagerService().setForcedDisplayDensity(displayId, density);
                } catch (RemoteException e) {
                    Log.w("DisplayDensityUtils", "Unable to save forced display density setting");
                }
            }
        });
    }
}
