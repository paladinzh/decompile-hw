package com.huawei.systemmanager.hsmstat.perioddata;

import android.content.Context;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import java.util.List;

public interface IPeriodData {
    long getIntervalTime();

    List<StatEntry> getRecordData(Context context);

    String getSharePreferenceKey();
}
