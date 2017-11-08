package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class MonthOverLimitNotifyPrefer extends AbsListPreference {
    private static final String TAG = "MonthOverLimitNotifyPrefer";
    private ArrayList<String> mOverFlow;
    private Runnable mRefreshSummaryTask = new Runnable() {
        public void run() {
            if (MonthOverLimitNotifyPrefer.this.mCard == null) {
                HwLog.e(MonthOverLimitNotifyPrefer.TAG, "mRefreshSummaryTask mCard == null");
                return;
            }
            int excessMonthType = MonthOverLimitNotifyPrefer.this.mCard.getExcessMonthType();
            DataHolder data = new DataHolder();
            data.excessMonthType = excessMonthType;
            data.summary = Util.getOverFlowTypeString(excessMonthType, MonthOverLimitNotifyPrefer.this.mOverFlow);
            MonthOverLimitNotifyPrefer.this.postRunnableUI(new SetSummaryTask(data));
        }
    };

    private static class DataHolder {
        int excessMonthType;
        String summary;

        private DataHolder() {
        }
    }

    private class SetSummaryTask implements Runnable {
        private final DataHolder data;

        public SetSummaryTask(DataHolder data) {
            this.data = data;
        }

        public void run() {
            MonthOverLimitNotifyPrefer.this.setValue(String.valueOf(this.data.excessMonthType));
            MonthOverLimitNotifyPrefer.this.setSummary(this.data.summary);
        }
    }

    public MonthOverLimitNotifyPrefer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MonthOverLimitNotifyPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MonthOverLimitNotifyPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.content_overflow_month_notify_settings);
        setDialogTitle(R.string.content_overflow_month_notify_settings);
        this.mOverFlow = Lists.newArrayList();
        Util.initOverFlowArray(getContext(), this.mOverFlow);
        setEntries((CharSequence[]) this.mOverFlow.toArray(new CharSequence[2]));
        setEntryValues(R.array.over_flow_value);
        setmultiLineLayout();
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mRefreshSummaryTask);
    }

    protected boolean onValueChanged(Object newValue) {
        HwLog.d(TAG, "onValueChanged");
        if (this.mCard == null) {
            return false;
        }
        int intKey = 1;
        try {
            intKey = Integer.parseInt((String) newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mCard.setExcessMonthType(intKey);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(intKey - 1));
        HsmStat.statE(96, statParam);
        refreshPreferShow();
        return true;
    }

    public void setmultiLineLayout() {
        HwLog.d(TAG, "layout need change");
        setLayoutResource(R.layout.preference_status_multi);
    }
}
