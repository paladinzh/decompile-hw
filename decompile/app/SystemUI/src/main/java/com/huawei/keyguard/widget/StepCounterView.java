package com.huawei.keyguard.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.StepCounterMonitor;
import com.huawei.keyguard.util.HwLog;

public class StepCounterView extends TextView {
    private static String TAG = "StepCounterView";
    HwUpdateCallback mHwUpdateCallback = new HwUpdateCallback() {
        public void onStepCounterChange(StepCounterInfo info) {
            if (info == null) {
                HwLog.w(StepCounterView.TAG, "onStepCounterChange info is null - no change happened!");
                return;
            }
            final int stepsNum = info.getStepsCount();
            HwLog.d(StepCounterView.TAG, "onStepCounterChange stepsNum:" + stepsNum);
            if (stepsNum > 0) {
                StepCounterView.this.post(new Runnable() {
                    public void run() {
                        StepCounterView.this.setVisibility(0);
                        StepCounterView.this.setText(String.valueOf(stepsNum));
                    }
                });
            } else {
                StepCounterView.this.setVisibility(8);
            }
        }
    };
    private boolean mPermanentHide = false;

    public StepCounterView(Context context) {
        super(context);
    }

    public StepCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StepCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!StepCounterMonitor.getHasHealthPackage()) {
            setVisibility(8);
        }
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mHwUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mHwUpdateCallback);
    }

    public void setPermanentHide(boolean hide) {
        this.mPermanentHide = hide;
        if (this.mPermanentHide && getVisibility() == 0) {
            super.setVisibility(8);
        }
    }

    public void setVisibility(int visibility) {
        if (StepCounterInfo.getInst().getStepsCount() == 0) {
            visibility = 8;
        }
        if (this.mPermanentHide) {
            visibility = 8;
        }
        super.setVisibility(visibility);
    }
}
