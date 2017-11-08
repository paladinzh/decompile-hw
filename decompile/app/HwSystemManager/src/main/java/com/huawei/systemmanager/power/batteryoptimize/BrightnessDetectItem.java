package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class BrightnessDetectItem extends PowerDetectItem {
    private static final String TAG = "BrightnessDetectItem";

    public String getTitle() {
        String paramPercent = NumberLocationPercent.getPercentage((double) DevStatusUtil.getScreenBrightnessPercent(getContext(), DevStatusUtil.getScreenBrightnessState(getContext())), 0);
        if (isOptimized()) {
            String str;
            if (DevStatusUtil.isBrightnessAutoOptimizeState(getContext())) {
                str = String.format(getContext().getResources().getString(R.string.power_brightness_optimize_des), new Object[]{Integer.valueOf(param)});
            } else {
                str = String.format(getContext().getResources().getString(R.string.power_brightness_suitable_des_format_s), new Object[]{paramPercent});
            }
            return str;
        }
        return String.format(getContext().getResources().getString(R.string.power_brightness_des_format_s), new Object[]{paramPercent});
    }

    public int getItemType() {
        return 5;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isBrightnessOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isBrightnessOptimzeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "brightness doOptimize called");
        DevStatusUtil.setScreenAutoBrightnessState(getContext(), true);
        setState(3);
    }
}
