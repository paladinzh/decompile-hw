package com.huawei.systemmanager.hsmstat.perioddata;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.CloudStat;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.rainbow.recommend.RecommendDataMgr;
import java.util.List;

public class CloudRecommendPeriodData implements IPeriodData {

    private static class ValueToStatEntryFunction implements Function<String, StatEntry> {
        private ValueToStatEntryFunction() {
        }

        public StatEntry apply(String input) {
            return new StatEntry(CloudStat.ACTION_RECOMMEND, input);
        }
    }

    public String getSharePreferenceKey() {
        return CloudStat.KEY_LAST_REPORT_RECOMMEND_DATE;
    }

    public long getIntervalTime() {
        return 604800000;
    }

    public List<StatEntry> getRecordData(Context ctx) {
        return Lists.newArrayList(Collections2.transform(RecommendDataMgr.collectRecommendBIData(ctx), new ValueToStatEntryFunction()));
    }
}
