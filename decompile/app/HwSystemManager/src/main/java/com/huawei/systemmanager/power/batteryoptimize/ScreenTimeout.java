package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class ScreenTimeout extends PowerDetectItem {
    private static final int FIFTEEN_SECONDS = 15;
    private static final int MILLI_SECOND = 1000;
    private static final int SECOND_PER_MINUTE = 60;
    private static final String TAG = "ScreenTimeout";
    private static final int TEN_MINUTES = 10;

    public String getTitle() {
        if (isOptimized()) {
            return String.format(getContext().getResources().getString(R.string.power_screentimeout_optimize_des_new), new Object[]{Integer.valueOf(15)});
        }
        String str;
        int mScreenTimeOutRestoreState = (int) DevStatusUtil.getScreenTimeoutState(getContext());
        int minuteNum = 0;
        if (mScreenTimeOutRestoreState / 1000 >= 60) {
            minuteNum = (mScreenTimeOutRestoreState / 1000) / 60;
        }
        if (minuteNum <= 0) {
            int second = mScreenTimeOutRestoreState / 1000;
            str = String.format(getContext().getResources().getString(R.string.power_screentimeout_des_new), new Object[]{Integer.valueOf(second), Integer.valueOf(15)});
        } else if (minuteNum > 10) {
            str = String.format(getContext().getResources().getString(R.string.power_screentimeout_never_des), new Object[]{Integer.valueOf(15)});
        } else {
            str = String.format(getContext().getResources().getString(R.string.power_screentimeout_minute_des_new), new Object[]{Integer.valueOf(minuteNum), Integer.valueOf(15)});
        }
        return str;
    }

    public int getItemType() {
        return 6;
    }

    public void doScan() {
        boolean on = DevStatusUtil.isScreenTimeoutOptimzeState(getContext());
        if (on) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isScreenTimeoutOptimzeStatet is " + on);
    }

    public void doOptimize() {
        HwLog.i(TAG, "ScreenTimeout doOptimize called");
        DevStatusUtil.setScreenTimeoutState(getContext(), 15000);
        setState(3);
    }
}
