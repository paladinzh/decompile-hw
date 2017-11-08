package com.android.contacts.util;

import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.common.collect.Lists;
import java.util.ArrayList;

public class StopWatch {
    private final String mLabel;
    private final ArrayList<String> mLapLabels = Lists.newArrayList();
    private final ArrayList<Long> mTimes = Lists.newArrayList();

    private StopWatch(String label) {
        this.mLabel = label;
        lap("");
    }

    public static StopWatch start(String label) {
        return new StopWatch(label);
    }

    public void lap(String lapLabel) {
        this.mTimes.add(Long.valueOf(System.currentTimeMillis()));
        this.mLapLabels.add(lapLabel);
    }

    public void stopAndLog(String TAG, int timeThresholdToLog) {
        lap("");
        long start = ((Long) this.mTimes.get(0)).longValue();
        long total = ((Long) this.mTimes.get(this.mTimes.size() - 1)).longValue() - start;
        if (total >= ((long) timeThresholdToLog)) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mLabel);
            sb.append(",");
            sb.append(total);
            sb.append(": ");
            long last = start;
            for (int i = 1; i < this.mTimes.size(); i++) {
                long current = ((Long) this.mTimes.get(i)).longValue();
                sb.append((String) this.mLapLabels.get(i));
                sb.append(",");
                sb.append(current - last);
                sb.append(HwCustPreloadContacts.EMPTY_STRING);
                last = current;
            }
            if (HwLog.HWDBG) {
                HwLog.v(TAG, sb.toString());
            }
        }
    }
}
