package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.util.AttributeSet;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class RoamingOverLimitNotifyPrefer extends AbsListPreference {
    public static final String TAG = "RoamingOverLimitNotifyPrefer";
    private ArrayList<String> mOverFlow;
    private Runnable mRefreshSummaryTask = new Runnable() {
        public void run() {
            if (RoamingOverLimitNotifyPrefer.this.mCard == null) {
                HwLog.e(RoamingOverLimitNotifyPrefer.TAG, "mRefreshSummaryTask mCard == null");
                return;
            }
            int excessRoamingType = RoamingOverLimitNotifyPrefer.this.mCard.getExcessRoamingType();
            DataHolder data = new DataHolder();
            data.excessRoamingType = excessRoamingType;
            data.summary = Util.getOverFlowTypeString(excessRoamingType, RoamingOverLimitNotifyPrefer.this.mOverFlow);
            RoamingOverLimitNotifyPrefer.this.postRunnableUI(new SetSummaryTask(data));
        }
    };

    private static class DataHolder {
        int excessRoamingType;
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
            RoamingOverLimitNotifyPrefer.this.setValue(String.valueOf(this.data.excessRoamingType));
            RoamingOverLimitNotifyPrefer.this.setSummary(this.data.summary);
        }
    }

    public RoamingOverLimitNotifyPrefer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RoamingOverLimitNotifyPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RoamingOverLimitNotifyPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.content_roaming_overflow_notify_settings);
        setDialogTitle(R.string.content_roaming_overflow_notify_settings);
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
        if (this.mCard == null) {
            return false;
        }
        int intKey = 1;
        try {
            intKey = Integer.parseInt((String) newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mCard.setExcessRoamingType(intKey);
        refreshPreferShow();
        return true;
    }

    public void setmultiLineLayout() {
        HwLog.d(TAG, "layout need change");
        setLayoutResource(R.layout.preference_status_multi);
    }
}
