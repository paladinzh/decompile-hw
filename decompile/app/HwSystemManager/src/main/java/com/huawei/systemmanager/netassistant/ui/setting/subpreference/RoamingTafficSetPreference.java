package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class RoamingTafficSetPreference extends BaseTrafficSetPreference {
    public static final String TAG = "RoamingTafficSetPreference";
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (RoamingTafficSetPreference.this.mCard == null) {
                HwLog.e(RoamingTafficSetPreference.TAG, "mLoadSummaryTask, mCard == null");
                return;
            }
            RoamingTafficSetPreference.this.postSetSummary(CommonMethodUtil.formatBytes(RoamingTafficSetPreference.this.getContext(), RoamingTafficSetPreference.this.mCard.getRoamingTraffic()));
        }
    };

    public RoamingTafficSetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RoamingTafficSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.roaming_traffic_title);
        setDialogTitle(R.string.roaming_traffic_title);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    protected void onSetPackage(long size) {
        if (this.mCard == null) {
            HwLog.e(TAG, "onSetPackage, mCard == null");
            return;
        }
        this.mCard.setRoamingTraffic(size);
        refreshPreferShow();
    }

    protected long getEditTxtValue() {
        return getRoamingTraffic();
    }

    private long getRoamingTraffic() {
        if (this.mCard == null) {
            return -1;
        }
        return this.mCard.getRoamingTraffic();
    }
}
