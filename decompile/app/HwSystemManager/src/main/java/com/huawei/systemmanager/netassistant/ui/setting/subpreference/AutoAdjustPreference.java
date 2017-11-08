package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.util.AttributeSet;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class AutoAdjustPreference extends AbsListPreference {
    private static final int ATUO_ADJUST_1DAY = 1;
    private static final int ATUO_ADJUST_3DAY = 2;
    private static final int ATUO_ADJUST_7DAY = 3;
    private static final int ATUO_ADJUST_OFF = 0;
    public static final String TAG = "AutoAdjustPreference";
    private ArrayList<String> mAutoAdjust;
    private Runnable mRefreshDataTask = new Runnable() {
        public void run() {
            if (AutoAdjustPreference.this.mCard == null) {
                HwLog.e(AutoAdjustPreference.TAG, "mRefreshDataTask mCard == null");
                return;
            }
            int mRegularAdjustType = AutoAdjustPreference.this.mCard.getSettingRegularAdjustType();
            String summary = AutoAdjustPreference.this.getRegularTypeString(mRegularAdjustType, AutoAdjustPreference.this.mAutoAdjust);
            DataHolder data = new DataHolder();
            data.regularAdjustType = mRegularAdjustType;
            data.summary = summary;
            AutoAdjustPreference.this.postRunnableUI(new SetSummaryTask(data));
        }
    };

    private static class DataHolder {
        int regularAdjustType;
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
            AutoAdjustPreference.this.setValue(String.valueOf(this.data.regularAdjustType));
            AutoAdjustPreference.this.setSummary(this.data.summary);
        }
    }

    public AutoAdjustPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AutoAdjustPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoAdjustPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setLayoutResource(R.layout.preference_status_3);
        setKey(TAG);
        setTitle(R.string.content_auto_adjust_settings);
        setDialogTitle(R.string.content_auto_adjust_settings);
        this.mAutoAdjust = Lists.newArrayList();
        initList();
        setEntries((CharSequence[]) this.mAutoAdjust.toArray(new CharSequence[4]));
        setEntryValues(R.array.auto_calibration_value);
        setSummary2((int) R.string.net_assistant_autoadjust_summary2);
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mRefreshDataTask);
    }

    protected boolean onValueChanged(Object newValue) {
        int value = 0;
        try {
            value = Integer.parseInt((String) newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mCard == null) {
            return false;
        }
        this.mCard.setSettingRegularAdjustType(value);
        refreshPreferShow();
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(value));
        HsmStat.statE(93, statParam);
        return true;
    }

    private String getRegularTypeString(int valueType, ArrayList<String> stringList) {
        HwLog.d(TAG, "getRegularTypeString and valueType is " + valueType);
        switch (valueType) {
            case 0:
                return (String) stringList.get(0);
            case 1:
                return (String) stringList.get(1);
            case 3:
                return (String) stringList.get(2);
            case 7:
                return (String) stringList.get(3);
            default:
                HwLog.d(TAG, "In default case ATUO_ADJUST_OFF");
                return (String) stringList.get(0);
        }
    }

    private void initList() {
        Context ctx = getContext();
        this.mAutoAdjust.clear();
        this.mAutoAdjust.add(getContext().getString(R.string.sub_content_auto_adjust_off));
        this.mAutoAdjust.add(ctx.getString(R.string.sub_content_auto_adjust_1day));
        this.mAutoAdjust.add(ctx.getString(R.string.sub_content_auto_adjust_3day));
        this.mAutoAdjust.add(ctx.getString(R.string.sub_content_auto_adjust_7day));
    }
}
