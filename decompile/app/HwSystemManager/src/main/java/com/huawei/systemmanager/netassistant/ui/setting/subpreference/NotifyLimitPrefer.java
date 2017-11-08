package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.util.HwLog;

public class NotifyLimitPrefer extends BaseTrafficSetPreference {
    public static final String TAG = "NotifyLimitPrefer";
    private boolean isNetAssistantEnable;
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (NotifyLimitPrefer.this.mCard == null) {
                HwLog.e(NotifyLimitPrefer.TAG, "mLoadSummaryTask mCard == null");
                return;
            }
            NotifyLimitPrefer.this.postSetSummary(CommonMethodUtil.formatBytes(NotifyLimitPrefer.this.getContext(), NotifyLimitPrefer.this.getMonthLimitNotifyBytes()));
        }
    };

    public NotifyLimitPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NotifyLimitPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.net_assistant_setting_traffic_month_title);
        setDialogTitle(R.string.net_assistant_setting_traffic_month_title);
        this.isNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        if (this.isNetAssistantEnable) {
            setLayoutResource(R.layout.preference_status_3);
            setSummary2((int) R.string.month_total_setting_summary);
            return;
        }
        setLayoutResource(R.layout.preference_status_multi);
    }

    protected void onSetPackage(long size) {
        if (this.mCard == null) {
            HwLog.e(TAG, "onSetPackage mCard == null");
            return;
        }
        this.mCard.setMonthNotifyLimit(size);
        callValueChanged(Long.valueOf(size));
        refreshPreferShow();
    }

    protected long getEditTxtValue() {
        return getMonthLimitNotifyBytes();
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    private long getMonthLimitNotifyBytes() {
        if (this.mCard == null) {
            return -1;
        }
        return this.mCard.getMonthLimitNotifyBytes();
    }
}
