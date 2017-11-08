package com.android.gallery3d.data;

public enum TimeBucketPageViewMode {
    DAY {
        public TimeBucketPageViewMode getUpdateMode(boolean beBiggerView) {
            return beBiggerView ? DAY : MONTH;
        }

        public String getNormalizedDateFormat() {
            return "strftime('%%Y%%m%%d', %s / 1000, 'unixepoch', 'localtime') AS normalized_date";
        }
    },
    MONTH {
        public TimeBucketPageViewMode getUpdateMode(boolean beBiggerView) {
            return beBiggerView ? DAY : MONTH;
        }

        public String getNormalizedDateFormat() {
            return "strftime('%%Y%%m', %s / 1000, 'unixepoch', 'localtime') AS normalized_date";
        }
    };

    public abstract String getNormalizedDateFormat();

    public abstract TimeBucketPageViewMode getUpdateMode(boolean z);

    public boolean beBiggerView(TimeBucketPageViewMode mode) {
        if (mode == DAY && this == MONTH) {
            return true;
        }
        return false;
    }
}
