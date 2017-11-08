package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSettingsActivity;
import com.huawei.systemmanager.util.HwLog;

public class LeisureTrafficSetPrefer extends AbsPreference {
    public static final String TAG = "LeisureTrafficSetPrefer";
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (LeisureTrafficSetPrefer.this.mCard == null) {
                HwLog.e(LeisureTrafficSetPrefer.TAG, "mLoadSummaryTask mCard == null");
                return;
            }
            LeisureTrafficSetPrefer.this.postSetSummary(LeisureTrafficSetPrefer.this.mCard.getLeisureTrafficDes(LeisureTrafficSetPrefer.this.getContext()));
        }
    };

    public LeisureTrafficSetPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LeisureTrafficSetPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.leisure_time_traffic_title);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    protected void onClick() {
        if (this.mCard == null) {
            HwLog.e(TAG, "onClick mCard == null");
            return;
        }
        Context ctx = getContext();
        Intent i = new Intent(ctx, LeisureTrafficSettingsActivity.class);
        i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mCard.getImsi());
        try {
            ctx.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
