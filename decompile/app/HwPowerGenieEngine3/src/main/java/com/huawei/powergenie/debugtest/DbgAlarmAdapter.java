package com.huawei.powergenie.debugtest;

import android.content.Context;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.AlarmAdapter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DbgAlarmAdapter extends DbgBaseAdapter {
    private final AlarmAdapter mPendingAdapter;
    private ArrayList<String> mPkgList = new ArrayList<String>() {
        {
            add("com.huawei.superassistant");
        }
    };

    DbgAlarmAdapter(Context context) {
        this.mPendingAdapter = AlarmAdapter.getInstance(context);
    }

    protected void startTest(PrintWriter pw) {
        super.startTest(pw);
        Log.i("DbgAlarmAdapter", "Alarm Adapter Test!");
        pw.println("\nAlarm Adapter Test!");
        if (this.mPendingAdapter != null) {
            printlnResult("pendingAppAlarms", getResult(this.mPendingAdapter.pendingAppAlarms(this.mPkgList, false)));
            printlnResult("unpendingAppAlarms", getResult(this.mPendingAdapter.unpendingAppAlarms(this.mPkgList, false)));
            printlnResult("unpendingAllAlarms", getResult(this.mPendingAdapter.unpendingAllAlarms()));
            printlnResult("periodAdjustAlarms", getResult(this.mPendingAdapter.periodAdjustAlarms(this.mPkgList, 0, 300000, 0)));
            printlnResult("removePeriodAdjustAlarms", getResult(this.mPendingAdapter.removePeriodAdjustAlarms(this.mPkgList, 0)));
            printlnResult("removeAllPeriodAdjustAlarms", getResult(this.mPendingAdapter.removeAllPeriodAdjustAlarms()));
        }
    }
}
