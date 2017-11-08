package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;

public class PackageLimitSetPreference extends BaseTrafficSetPreference {
    public static final String TAG = "PackageSettingPreference";
    private Runnable mLoadDataTask = new Runnable() {
        public void run() {
            if (PackageLimitSetPreference.this.mCard == null) {
                HwLog.e(PackageLimitSetPreference.TAG, "mLoadDataTask card is null!");
                return;
            }
            PackageLimitSetPreference.this.postSetSummary(CommonMethodUtil.formatBytes(PackageLimitSetPreference.this.getContext(), PackageLimitSetPreference.this.getPackgeFlowLimit()));
        }
    };

    public PackageLimitSetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PackageLimitSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.net_assistant_packageflow_limit);
        setDialogTitle(R.string.net_assistant_packageflow_limit);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadDataTask);
    }

    protected void onSetPackage(long size) {
        if (this.mCard != null) {
            this.mCard.setPackgeFlowLimit(size);
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.KEY_TRAFFIC_LIMIT, String.valueOf(size));
            HsmStat.statE(91, statParam);
            refreshPreferShow();
        }
    }

    protected long getEditTxtValue() {
        return getPackgeFlowLimit();
    }

    private long getPackgeFlowLimit() {
        if (this.mCard == null) {
            return -1;
        }
        return this.mCard.getPackageFlowLimit();
    }
}
