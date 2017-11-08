package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class ExtraTrafficPreference extends BaseTrafficSetPreference {
    public static final String TAG = "ExtraTrafficPreference";
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (ExtraTrafficPreference.this.mCard == null) {
                HwLog.e(ExtraTrafficPreference.TAG, "mLoadSummaryTask mCard == null");
                return;
            }
            ExtraTrafficPreference.this.postSetSummary(CommonMethodUtil.formatBytes(ExtraTrafficPreference.this.getContext(), ExtraTrafficPreference.this.getExtraTraffic()));
        }
    };

    public ExtraTrafficPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtraTrafficPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.extra_traffic_package_title);
        setDialogTitle(R.string.extra_traffic_package_title);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    protected void onSetPackage(long size) {
        if (this.mCard != null) {
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, Formatter.formatFileSize(GlobalContext.getContext(), size));
            HsmStat.statE((int) Events.E_NETASSISTANT_SET_EXTRA_SIZE, statParam);
            this.mCard.setExtraTraffic(size);
            refreshPreferShow();
        }
    }

    protected long getEditTxtValue() {
        return getExtraTraffic();
    }

    private long getExtraTraffic() {
        if (this.mCard != null) {
            return this.mCard.getExtraTraffic();
        }
        HwLog.e(TAG, "getExtraTraffic mCard == null");
        return -1;
    }
}
