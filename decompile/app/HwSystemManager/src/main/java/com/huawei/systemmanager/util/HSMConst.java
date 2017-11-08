package com.huawei.systemmanager.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.GridView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;

public class HSMConst {
    public static final float DEVICE_SIZE_100 = 10.0f;
    public static final float DEVICE_SIZE_55 = 5.5f;
    public static final float DEVICE_SIZE_80 = 8.0f;
    private static final String ENABLE_NAVBAR = "enable_navbar";
    public static final int GRIDIVIEW_COLUMN_NO_LANDSCAPE = 2;
    public static final int GRIDIVIEW_COLUMN_NO_PORTRAIT = 1;
    public static final int NUOYI_ALL = 12;
    public static final int NUOYI_PART = 5;
    public static final int SPLIT_INTENT_HWFLAG_AND_SPLIT_NOT = 0;
    private static final String TAG = "HSMConst";
    public static final int VIRTUAL_NAVI_HIDE = 0;
    public static final int VIRTUAL_NAVI_SHOW = 1;
    private static int shortSide = 0;

    public static int getDimensionPixelSize(int id) {
        return GlobalContext.getContext().getResources().getDimensionPixelSize(id);
    }

    public static int getVirtualNaviState(int defValue) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        if (cr != null) {
            return System.getIntForUser(cr, ENABLE_NAVBAR, defValue, -2);
        }
        HwLog.i(TAG, "getContentResolver return null");
        return defValue;
    }

    public static boolean isLand() {
        boolean z = false;
        if (!Utility.isSupportOrientation()) {
            return false;
        }
        if (GlobalContext.getContext().getResources().getConfiguration().orientation == 2) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void doMultiply(Context context, boolean land, GridView gv) {
        if (gv != null && gv.getAdapter() != null && gv.getAdapter().getCount() >= 2 && getDeviceSize() >= DEVICE_SIZE_80) {
            if (land) {
                gv.setNumColumns(2);
            } else {
                gv.setNumColumns(1);
            }
        }
    }

    public static void setCfgForSlidingUp(Intent intent, SlidingUpPanelLayout view) {
        boolean z = false;
        if (intent == null) {
            HwLog.i(TAG, "setCfgForSlidingUp, intent is null");
        } else if (view == null) {
            HwLog.i(TAG, "setCfgForSlidingUp, SlidingUpPanelLayout is null");
        } else {
            if ((intent.getHwFlags() & 4) == 0) {
                z = true;
            }
            view.setSupportConfigurationChange(z);
        }
    }

    public static float getDeviceSize() {
        DisplayMetrics dm = getDisplayMetrics();
        return (float) Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d));
    }

    public static boolean isSupportSubfiled(Context context) {
        return getDeviceSize() >= DEVICE_SIZE_80;
    }

    public static int judgeSplitModeByDeviceSize(float f) {
        if (f < DEVICE_SIZE_55) {
            return 0;
        }
        if (f < DEVICE_SIZE_80) {
            return 1;
        }
        if (f < DEVICE_SIZE_100) {
            return 2;
        }
        return 3;
    }

    public static void managerBundle(Bundle bundle) {
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                if ("android:fragments".equals(key)) {
                    bundle.remove(key);
                    return;
                }
            }
        }
    }

    public static int getActionBarHeight() {
        TypedArray actionBarSizeTypedArray = GlobalContext.getContext().obtainStyledAttributes(new int[]{16843499});
        if (actionBarSizeTypedArray == null) {
            return 0;
        }
        int height = actionBarSizeTypedArray.getDimensionPixelSize(0, 0);
        actionBarSizeTypedArray.recycle();
        return height;
    }

    public static int getLongOrShortLength(boolean isLong) {
        if (isLong) {
            DisplayMetrics dm = getDisplayMetrics();
            int longSide = dm.heightPixels > dm.widthPixels ? dm.heightPixels : dm.widthPixels;
            if (getVirtualNaviState(0) == 1) {
                longSide += getDimensionPixelSize(R.dimen.virtual_navigator_height);
            }
            return longSide;
        }
        if (shortSide == 0) {
            dm = getDisplayMetrics();
            shortSide = dm.heightPixels < dm.widthPixels ? dm.heightPixels : dm.widthPixels;
        }
        return shortSide;
    }

    public static int getNuoyiLeftWidth() {
        return (getLongOrShortLength(true) * 5) / 12;
    }

    public static DisplayMetrics getDisplayMetrics() {
        Display display = ((WindowManager) GlobalContext.getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics;
    }
}
