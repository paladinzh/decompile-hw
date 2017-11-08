package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class FeedBackDetectitem extends PowerDetectItem {
    private static final String TAG = "FeedBackDetectitem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_feedback_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_feedback_des);
    }

    public int getItemType() {
        return 11;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isTouchFeedbackOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isTouchFeedbackOptimzeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "FeedBack doOptimize called");
        DevStatusUtil.setTouchFeedbackState(getContext(), false);
        setState(3);
    }
}
