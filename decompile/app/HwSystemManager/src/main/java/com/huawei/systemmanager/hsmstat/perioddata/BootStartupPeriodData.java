package com.huawei.systemmanager.hsmstat.perioddata;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.startupmgr.comm.AbsRecordInfo;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import java.util.List;

public class BootStartupPeriodData implements IPeriodData {
    private static final String KEY_LAST_REPORT_BOOT_UP = "last_boot_up_date";
    private static final String PARAM_ALLOW_COUNT = "ac";
    private static final String PARAM_FORBID_COUNT = "fc";

    public String getSharePreferenceKey() {
        return KEY_LAST_REPORT_BOOT_UP;
    }

    public long getIntervalTime() {
        return 86400000;
    }

    public List<StatEntry> getRecordData(Context ctx) {
        List<StatEntry> result = Lists.newArrayList();
        result.add(autoStartupStatEntry(ctx));
        result.add(awakedStartupStatEntry(ctx));
        result.add(normalRecordStatEntry(ctx));
        result.add(awakedRecordStatEntry(ctx));
        return result;
    }

    StatEntry autoStartupStatEntry(Context ctx) {
        int allowCount = 0;
        int forbidCount = 0;
        for (NormalStartupInfo info : StartupDataMgrHelper.queryNormalStartupInfoList(ctx)) {
            if (info.getStatus()) {
                allowCount++;
            } else {
                forbidCount++;
            }
        }
        return new StatEntry(String.valueOf(1002), HsmStatConst.constructValue("ac", String.valueOf(allowCount), PARAM_FORBID_COUNT, String.valueOf(forbidCount)));
    }

    StatEntry awakedStartupStatEntry(Context ctx) {
        int allowCount = 0;
        int forbidCount = 0;
        for (AwakedStartupInfo info : StartupDataMgrHelper.queryAwakedStartupInfoList(ctx)) {
            if (info.getStatus()) {
                allowCount++;
            } else {
                forbidCount++;
            }
        }
        return new StatEntry(String.valueOf(Events.E_STARTUPMGR_AWAKED_STARTUP_PERIOD_STATISTICS), HsmStatConst.constructValue("ac", String.valueOf(allowCount), PARAM_FORBID_COUNT, String.valueOf(forbidCount)));
    }

    StatEntry normalRecordStatEntry(Context ctx) {
        return buildStatEntry(StartupDataMgrHelper.queryNormalRecordInfoList(ctx), 1008);
    }

    StatEntry awakedRecordStatEntry(Context ctx) {
        return buildStatEntry(StartupDataMgrHelper.queryAwakedRecordInfoList(ctx), 1009);
    }

    private StatEntry buildStatEntry(List<? extends AbsRecordInfo> records, int events) {
        StringBuffer results = new StringBuffer();
        for (AbsRecordInfo record : records) {
            results.append(record.getRecordInfo());
        }
        int len = results.length();
        return new StatEntry(String.valueOf(events), len > 0 ? results.substring(0, len - 1) : "");
    }
}
