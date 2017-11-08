package com.android.systemui.utils.analyze;

import android.util.Jlog;
import android.util.SparseArray;
import android.util.SparseLongArray;
import com.android.systemui.utils.HwLog;
import fyusion.vislib.BuildConfig;

public class JanklogUtils {
    private static SparseLongArray mConsumeCache = new SparseLongArray();
    private static SparseArray<JankRange> mJankBaseTime = new SparseArray();

    private static final class JankRange {
        private final long mCritical;
        private final long mMajor;
        private final long mMinor;
        private final long mThreshold;

        JankRange(long minor, long major, long critical, float ratio) {
            this.mCritical = critical;
            this.mMajor = major;
            this.mMinor = minor;
            this.mThreshold = (long) (((float) minor) * ratio);
        }

        boolean shouldReportJank(long curValue) {
            if (curValue >= this.mCritical) {
                HwLog.w("JanklogUtils", "will report critical jank log");
            } else if (curValue >= this.mMajor) {
                HwLog.w("JanklogUtils", "will report major jank log");
            } else if (curValue >= this.mMinor) {
                HwLog.w("JanklogUtils", "will report minor jank log");
            } else {
                HwLog.d("JanklogUtils", "shouldReportJank report un-jank");
            }
            if (this.mMinor >= curValue || curValue > this.mThreshold) {
                return false;
            }
            return true;
        }

        public String toString() {
            return "[" + this.mMinor + "," + this.mMajor + "," + this.mCritical + "]";
        }
    }

    static {
        mJankBaseTime.put(133, new JankRange(130, 160, 200, 2.0f));
        mJankBaseTime.put(135, new JankRange(600, 900, 1200, 2.5f));
    }

    public static void eventBegin(int jLogId) {
        synchronized (mConsumeCache) {
            mConsumeCache.put(jLogId, System.currentTimeMillis());
        }
    }

    public static void eventEnd(int jLogId, String msg) {
        synchronized (mConsumeCache) {
            long beginTime = mConsumeCache.get(jLogId);
            if (0 != beginTime) {
                log(jLogId, System.currentTimeMillis() - beginTime, msg);
            }
            mConsumeCache.clear();
        }
    }

    public static int perfEvent(int perfEventId) {
        HwLog.i("JanklogUtils", "perfEvent is called");
        return Jlog.perfEvent(perfEventId, BuildConfig.FLAVOR, new int[0]);
    }

    private static void log(int jLogId, long currentValue, String msg) {
        if (isNeedReport(jLogId, currentValue)) {
            HwLog.i("JanklogUtils", "jank log " + jLogId + ", " + msg + ", " + currentValue);
            Jlog.d(jLogId, (int) currentValue, msg + ", v:" + currentValue);
        }
    }

    private static boolean isNeedReport(int jLogId, long currentValue) {
        JankRange base = (JankRange) mJankBaseTime.get(jLogId);
        if (base != null) {
            return base.shouldReportJank(currentValue);
        }
        HwLog.w("JanklogUtils", "isNeedReport invalid baseTime for jLogId: " + jLogId);
        return false;
    }
}
