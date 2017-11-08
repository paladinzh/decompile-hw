package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppActivity;
import com.huawei.systemmanager.util.HwLog;

public class NoTafficAppPrefer extends AbsPreference {
    public static final String TAG = "NoTafficAppPrefer";
    private Runnable mLoadDataTask = new Runnable() {
        public void run() {
            if (NoTafficAppPrefer.this.mCard == null) {
                HwLog.e(NoTafficAppPrefer.TAG, "mLoadDataTask card is null!");
                return;
            }
            int count = NoTafficAppPrefer.this.mCard.getNoTrafficAppCount();
            NoTafficAppPrefer.this.postSetSummary(NoTafficAppPrefer.this.getContext().getResources().getQuantityString(R.plurals.app_cnt_suffix, count, new Object[]{Integer.valueOf(count)}));
        }
    };

    public NoTafficAppPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoTafficAppPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setLayoutResource(R.layout.preference_status_3);
        setTitle(R.string.no_traffic_app_title);
        setSummary2((int) R.string.net_assistant_no_trafficapp_summry2);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadDataTask);
    }

    protected void onClick() {
        if (this.mCard == null) {
            HwLog.e(TAG, "onClick card is null!");
            return;
        }
        Context ctx = getContext();
        Intent i = new Intent(ctx, NoTrafficAppActivity.class);
        i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mCard.getImsi());
        try {
            ctx.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
