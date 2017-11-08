package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;

public class DayUsedNotifyPrefer extends AbsSeekBarPrefer {
    public static final String TAG = "DayUsedNotifyPrefer";
    private Runnable mRefreshTask = new Runnable() {
        public void run() {
            if (DayUsedNotifyPrefer.this.mCard == null) {
                HwLog.e(DayUsedNotifyPrefer.TAG, "mRefreshTask, mCard == null");
                return;
            }
            int dailyPercent = DayUsedNotifyPrefer.this.mCard.getDiallyWarnPercent();
            String monthSummary = CommonMethodUtil.formatBytes(DayUsedNotifyPrefer.this.getContext(), (((long) dailyPercent) * DayUsedNotifyPrefer.this.mCard.getMonthLimitNotifyBytes()) / 100);
            String summary = DayUsedNotifyPrefer.this.getContext().getString(R.string.net_assistant_setting_daily_notify_summary, new Object[]{CommonMethodUtil.formatPercentString(dailyPercent), monthSummary});
            DataHolder data = new DataHolder();
            data.summary = summary;
            data.monthPercent = dailyPercent;
            DayUsedNotifyPrefer.this.postRunnableUI(new SetSummaryTask(data));
        }
    };

    private static class DataHolder {
        int monthPercent;
        String summary;

        private DataHolder() {
        }
    }

    private class SetSummaryTask implements Runnable {
        private final DataHolder mDataHoldre;

        public SetSummaryTask(DataHolder dataHolder) {
            this.mDataHoldre = dataHolder;
        }

        public void run() {
            DayUsedNotifyPrefer.this.setSummary(this.mDataHoldre.summary);
            DayUsedNotifyPrefer.this.setDefaultProgress(this.mDataHoldre.monthPercent);
        }
    }

    public DayUsedNotifyPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DayUsedNotifyPrefer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.net_assistant_setting_daily_notify_title);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mRefreshTask);
    }

    protected boolean onValueChanged(Object newValue) {
        if (this.mCard == null) {
            return false;
        }
        int percent = -1;
        try {
            percent = ((Integer) newValue).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (percent < 0) {
            return false;
        }
        this.mCard.setDiallyWarnPercent(percent);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(percent));
        HsmStat.statE(98, statParam);
        refreshPreferShow();
        return true;
    }
}
