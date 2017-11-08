package com.android.gallery3d.data;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import java.util.ArrayList;

/* compiled from: TimeClustering */
class Cluster {
    public boolean mGeographicallySeparatedFromPrevCluster = false;
    private ArrayList<SmallItem> mItems = new ArrayList();

    public void addItem(SmallItem item) {
        this.mItems.add(item);
    }

    public int size() {
        return this.mItems.size();
    }

    public SmallItem getLastItem() {
        int n = this.mItems.size();
        return n == 0 ? null : (SmallItem) this.mItems.get(n - 1);
    }

    public ArrayList<SmallItem> getItems() {
        return this.mItems;
    }

    public String generateCaption(Context context) {
        int n = this.mItems.size();
        String caption;
        if (TimeClustering.USE_YYYYMM_ALGO) {
            if (n == 0) {
                caption = "";
            } else {
                caption = DateUtils.formatDateTime(context, ((SmallItem) this.mItems.get(0)).dateInMs, 36);
            }
            return caption;
        }
        long minTimestamp = 0;
        long maxTimestamp = 0;
        for (int i = 0; i < n; i++) {
            long t = ((SmallItem) this.mItems.get(i)).dateInMs;
            if (t != 0) {
                if (minTimestamp == 0) {
                    maxTimestamp = t;
                    minTimestamp = t;
                } else {
                    minTimestamp = Math.min(minTimestamp, t);
                    maxTimestamp = Math.max(maxTimestamp, t);
                }
            }
        }
        if (minTimestamp == 0) {
            return "";
        }
        String minDay = DateFormat.format("MMddyy", minTimestamp).toString();
        String maxDay = DateFormat.format("MMddyy", maxTimestamp).toString();
        if (minDay.substring(4).equals(maxDay.substring(4))) {
            caption = DateUtils.formatDateRange(context, minTimestamp, maxTimestamp, 524288);
            if (minDay.equals(maxDay) && !DateUtils.formatDateTime(context, minTimestamp, 65552).equals(DateUtils.formatDateTime(context, minTimestamp, 65556))) {
                long midTimestamp = (minTimestamp + maxTimestamp) / 2;
                caption = DateUtils.formatDateRange(context, midTimestamp, midTimestamp, 65553);
            }
        } else {
            caption = DateUtils.formatDateRange(context, minTimestamp, maxTimestamp, 65584);
        }
        return caption;
    }
}
