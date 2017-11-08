package com.huawei.netassistant.calculator;

import android.net.NetworkTemplate;
import android.util.SparseArray;
import com.google.android.collect.Lists;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.NetworkSessionUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Iterator;

public class CalculateTrafficManager {
    private static CalculateTrafficManager mSingleton;
    private static final Object sMutexCalculateTrafficManager = new Object();
    private NetworkSessionUtil mStatsSession = new NetworkSessionUtil();

    public CalculateTrafficManager() {
        this.mStatsSession.openSession();
    }

    public static CalculateTrafficManager getInstance() {
        CalculateTrafficManager calculateTrafficManager;
        synchronized (sMutexCalculateTrafficManager) {
            if (mSingleton == null) {
                mSingleton = new CalculateTrafficManager();
            }
            calculateTrafficManager = mSingleton;
        }
        return calculateTrafficManager;
    }

    public static void destroyInstance() {
        synchronized (sMutexCalculateTrafficManager) {
            mSingleton = null;
        }
    }

    public NetworkSessionUtil getStatsSession() {
        return this.mStatsSession;
    }

    public long getTodaySpendTraffic(NetworkTemplate template) {
        return CalculateTrafficMethod.getTotalBytesInPeriod(template, DateUtil.getDayStartTimeMills(), DateUtil.getCurrentTimeMills());
    }

    public long getDaySpendTraffic(NetworkTemplate template, long dayStartMills, long dayEndMills) {
        return CalculateTrafficMethod.getTotalBytesInPeriod(template, dayStartMills, dayEndMills);
    }

    public long getMonthSpendTraffic(NetworkTemplate template, String imsi) {
        return CalculateTrafficMethod.getTotalBytesInPeriod(template, DateUtil.getMonthStartTimeMills(imsi), DateUtil.getCurrentTimeMills());
    }

    public long getLockScreenSpendTraffic(NetworkTemplate template, long start, long end) {
        return CalculateTrafficMethod.getTotalBytesInPeriod(template, start, end);
    }

    public long getCurrentTrafficForSpeedCheck(NetworkTemplate template, long now) {
        return CalculateTrafficMethod.getTotalBytesInPeriod(template, 0, now);
    }

    public ArrayList<ParcelableAppItem> setAllUidSpendTraffic(NetworkTemplate template, long start, long end) {
        return CalculateTrafficMethod.getAllUidBytesInPeriod(template, start, end, end);
    }

    public SparseArray<ParcelableAppItem> setAllUidSpendTrafficForCalc(NetworkTemplate template, long start, long end) {
        return CalculateTrafficMethod.getAllUidBytesInPeriodWithKey(template, start, end, end);
    }

    public ArrayList<ParcelableAppItem> setAllUidSpendTrafficInScreenLocking(SparseArray<ParcelableAppItem> oldList, ArrayList<ParcelableAppItem> newList) {
        ArrayList<ParcelableAppItem> itemsList = Lists.newArrayList();
        Iterator<ParcelableAppItem> localIterator = newList.iterator();
        while (localIterator.hasNext()) {
            ParcelableAppItem newItem = (ParcelableAppItem) localIterator.next();
            ParcelableAppItem oldItem = (ParcelableAppItem) oldList.get(newItem.key, null);
            ParcelableAppItem item = new ParcelableAppItem(newItem.key);
            if (oldItem != null) {
                item.backgroundbytes = newItem.backgroundbytes - oldItem.backgroundbytes;
                item.foregroundbytes = newItem.foregroundbytes - oldItem.foregroundbytes;
                item.mobiletotal = newItem.mobiletotal - oldItem.mobiletotal;
                item.wifitotal = newItem.wifitotal - oldItem.wifitotal;
            } else {
                item = newItem;
                HwLog.w("CalculateTrafficManager", "oldItem is null");
            }
            if (item.mobiletotal > 0 || item.wifitotal > 0) {
                itemsList.add(item);
            }
        }
        return itemsList;
    }

    public boolean needTrackTwice(NetworkTemplate template, long start, long end) {
        if (start < end) {
            return CalculateTrafficMethod.isTimeInLatestBucketDuration(template, start);
        }
        return false;
    }
}
