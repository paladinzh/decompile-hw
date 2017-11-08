package com.huawei.netassistant.calculator;

import android.app.ActivityManager;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.format.Formatter;
import android.util.SparseArray;
import com.google.android.collect.Lists;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.NetworkSessionUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class CalculateTrafficMethod {
    private static final String TAG = "CalculateTrafficMethod";

    public static long getTotalBytesInPeriod(NetworkTemplate template, long start, long end) {
        long iResult = 0;
        try {
            NetworkStats networkStats = getNetworkSession().getSummaryForNetwork(template, start, end);
            if (networkStats == null) {
                return 0;
            }
            Entry netEntry = networkStats.getTotalIncludingTags(null);
            iResult = netEntry.rxBytes + netEntry.txBytes;
            return iResult;
        } catch (RemoteException remoteException) {
            HwLog.e(TAG, "getTotalBytesInPeriod RemoteException:" + remoteException.getMessage());
        } catch (Exception exception) {
            HwLog.i(TAG, "getTotalBytesInPeriod Exception:" + exception.getMessage());
        }
    }

    public static long getBackGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) {
        if (template == null) {
            HwLog.w(TAG, "getBackGroundBytesByUid template is null");
            return 0;
        }
        for (ParcelableAppItem item : getAllUidBytesInPeriod(template, start, end, now)) {
            if (item.key == uid) {
                return item.backgroundbytes;
            }
        }
        return 0;
    }

    public static long getForeGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) {
        if (template == null) {
            HwLog.w(TAG, "getForeGroundBytesByUid template is null");
            return 0;
        }
        for (ParcelableAppItem item : getAllUidBytesInPeriod(template, start, end, now)) {
            if (item.key == uid) {
                return item.foregroundbytes;
            }
        }
        return 0;
    }

    public static long getTotalBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) {
        for (ParcelableAppItem item : getAllUidBytesInPeriod(template, start, end, now)) {
            if (item.key == uid) {
                return item.foregroundbytes + item.backgroundbytes;
            }
        }
        return 0;
    }

    private static NetworkSessionUtil getNetworkSession() {
        return CalculateTrafficManager.getInstance().getStatsSession();
    }

    public static ArrayList<ParcelableAppItem> getAllUidBytesInPeriod(NetworkTemplate template, long start, long end, long now) {
        HwLog.i(TAG, "getAllUidBytesInPeriod start = " + DateUtil.millisec2String(start) + " end = " + DateUtil.millisec2String(end));
        int currentUserId = ActivityManager.getCurrentUser();
        SparseArray<ParcelableAppItem> knownItems = new SparseArray();
        ArrayList<ParcelableAppItem> itemsList = Lists.newArrayList();
        NetworkStats stats = null;
        try {
            stats = getNetworkSession().getSummaryForAllUid(template, start, end, false);
        } catch (RemoteException remoteException) {
            HwLog.e(TAG, "getAllUidBytesInPeriod RemoteException:" + remoteException.getMessage());
        } catch (Exception exception) {
            HwLog.e(TAG, "getAllUidBytesInPeriod Exception:" + exception.getMessage());
        }
        Entry entry = null;
        if (stats == null) {
            return itemsList;
        }
        int size = stats.size();
        int i = 0;
        while (i < size) {
            try {
                entry = stats.getValues(i, entry);
                int uid = entry.uid;
                int collapseKey = collapseUidsTogether(uid, currentUserId);
                ParcelableAppItem item = (ParcelableAppItem) knownItems.get(collapseKey);
                if (item == null) {
                    item = new ParcelableAppItem(collapseKey);
                    knownItems.put(item.key, item);
                    itemsList.add(item);
                }
                item.setTrafficWithNoKey(uid, entry, template);
                i++;
            } catch (Exception ex) {
                HwLog.e(TAG, "getAllUidBytesInPeriod Exception:" + ex.getMessage());
            }
        }
        return itemsList;
    }

    public static SparseArray<ParcelableAppItem> getAllUidBytesInPeriodWithKey(NetworkTemplate template, long start, long end, long now) {
        int currentUserId = ActivityManager.getCurrentUser();
        SparseArray<ParcelableAppItem> knownItems = new SparseArray();
        NetworkStats stats = null;
        try {
            stats = getNetworkSession().getSummaryForAllUid(template, start, end, false);
        } catch (RemoteException remoteException) {
            HwLog.e(TAG, "getAllUidBytesInPeriodWithKey RemoteException:" + remoteException.getMessage());
        } catch (Exception exception) {
            HwLog.e(TAG, "getAllUidBytesInPeriodWithKey Exception:" + exception.getMessage());
        }
        Entry entry = null;
        if (stats == null) {
            return knownItems;
        }
        int size = stats.size();
        int i = 0;
        while (i < size) {
            try {
                entry = stats.getValues(i, entry);
                int uid = entry.uid;
                int collapseKey = collapseUidsTogether(uid, currentUserId);
                ParcelableAppItem item = (ParcelableAppItem) knownItems.get(collapseKey);
                if (item == null) {
                    item = new ParcelableAppItem(collapseKey);
                    knownItems.put(item.key, item);
                }
                item.setTrafficData(uid, entry, template);
                i++;
            } catch (Exception ex) {
                HwLog.e(TAG, "getAllUidBytesInPeriodWithKey Exception:" + ex.getMessage());
            }
        }
        return knownItems;
    }

    public static boolean isTimeInLatestBucketDuration(NetworkTemplate template, long start) {
        try {
            NetworkStatsHistory standard = getNetworkSession().getHistoryForNetwork(template, 10);
            if (standard != null && standard.getIndexBefore(start) == standard.size() - 1) {
                return true;
            }
        } catch (RemoteException remoteException) {
            HwLog.e(TAG, "isTimeInLatestBucketDuration RemoteException:" + remoteException.getMessage());
        } catch (Exception exception) {
            HwLog.e(TAG, "isTimeInLatestBucketDuration Exception:" + exception.getMessage());
        }
        return false;
    }

    public static HashSet<Integer> getUidSetWithTraffic(NetworkTemplate template, long start, long end) {
        HashSet<Integer> uidSet = new HashSet(256);
        NetworkStats stats = null;
        try {
            stats = getNetworkSession().getSummaryForAllUid(template, start, end, false);
        } catch (RemoteException remoteException) {
            HwLog.e(TAG, "getUidSetWithTraffic RemoteException:" + remoteException.getMessage());
        } catch (Exception exception) {
            HwLog.e(TAG, "getUidSetWithTraffic Exception:" + exception.getMessage());
        }
        Entry entry = null;
        if (stats == null) {
            return uidSet;
        }
        int size = stats.size();
        int i = 0;
        while (i < size) {
            try {
                entry = stats.getValues(i, entry);
                uidSet.add(Integer.valueOf(entry.uid));
                i++;
            } catch (Exception ex) {
                HwLog.e(TAG, "getUidSetWithTraffic Exception:" + ex.getMessage());
            }
        }
        return uidSet;
    }

    private static int collapseUidsTogether(int uid, int currentUserId) {
        if (UserHandle.getUserId(uid) != currentUserId) {
            return -(UserHandle.getUserId(uid) + Events.E_PERMISSION_RECOMMEND_CLICK);
        }
        if (UserHandle.isApp(uid) || uid == -4 || uid == -5) {
            return uid;
        }
        return 1000;
    }

    public static long getBackGroundBytesByAll(ArrayList<ParcelableAppItem> items) {
        long totalBackBytesForAll = 0;
        Iterator<ParcelableAppItem> localIterator = items.iterator();
        while (localIterator.hasNext()) {
            totalBackBytesForAll += ((ParcelableAppItem) localIterator.next()).backgroundbytes;
        }
        return totalBackBytesForAll;
    }

    public static long getForeGroundBytesByAll(ArrayList<ParcelableAppItem> items) {
        long totalForeBytesForAll = 0;
        Iterator<ParcelableAppItem> localIterator = items.iterator();
        while (localIterator.hasNext()) {
            ParcelableAppItem item = (ParcelableAppItem) localIterator.next();
            HwLog.v(TAG, "itemkey " + item.key + " Bytes:" + Formatter.formatFileSize(GlobalContext.getContext(), item.foregroundbytes));
            totalForeBytesForAll += item.foregroundbytes;
        }
        return totalForeBytesForAll;
    }
}
