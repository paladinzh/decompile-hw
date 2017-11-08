package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.util.HwLog;

public class NoAppTrafficAnalysis {
    private static final String TAG = "NoAppTrafficAnalysis";
    private static NoAppTrafficAnalysis sInstance;
    private String mImsi;
    private long mLastTotalBytes;
    private Object mLock = new Object();
    private SparseIntArray mUidList = new SparseIntArray();

    private NoAppTrafficAnalysis() {
    }

    public static synchronized NoAppTrafficAnalysis getDefault() {
        NoAppTrafficAnalysis noAppTrafficAnalysis;
        synchronized (NoAppTrafficAnalysis.class) {
            if (sInstance == null) {
                sInstance = new NoAppTrafficAnalysis();
            }
            noAppTrafficAnalysis = sInstance;
        }
        return noAppTrafficAnalysis;
    }

    public void init() {
        this.mImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        onUidListChanged();
        HwLog.d(TAG, "init normal traffic byte = " + this.mLastTotalBytes);
    }

    private long getAllNoTrafficAppBytes() {
        long totalBytes = 0;
        synchronized (this.mLock) {
            int size = this.mUidList.size();
            for (int i = 0; i < size; i++) {
                totalBytes += TrafficStats.getUidTxBytes(this.mUidList.valueAt(i)) + TrafficStats.getUidRxBytes(this.mUidList.valueAt(i));
                HwLog.i(TAG, "total byte = " + totalBytes + " uid = " + this.mUidList.valueAt(i));
            }
        }
        return totalBytes;
    }

    public long getDeltaTraffic() {
        long alertByte = getAllNoTrafficAppBytes();
        NetworkInfo networkInfo = ((ConnectivityManager) GlobalContext.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() != 0) {
            HwLog.d(TAG, "in other network, only update last byte, not alert delta");
            this.mLastTotalBytes = alertByte;
            return 0;
        } else if (alertByte == 0) {
            HwLog.d(TAG, "alert byte = 0, so return");
            this.mLastTotalBytes = alertByte;
            return 0;
        } else if (this.mLastTotalBytes == 0) {
            HwLog.d(TAG, "last alert byte = 0, so return");
            this.mLastTotalBytes = alertByte;
            return 0;
        } else {
            long delta = alertByte - this.mLastTotalBytes;
            if (delta < 0) {
                HwLog.d(TAG, "delta < 0, so return");
                this.mLastTotalBytes = alertByte;
                return 0;
            }
            this.mLastTotalBytes = alertByte;
            return delta;
        }
    }

    public void onUidListChanged() {
        NoTrafficAppDbInfo noTrafficAppDbInfo = new NoTrafficAppDbInfo(SimCardManager.getInstance().getPreferredDataSubscriberId());
        noTrafficAppDbInfo.initDbData();
        synchronized (this.mLock) {
            this.mUidList = noTrafficAppDbInfo.getNoTrafficList();
        }
        this.mLastTotalBytes = getAllNoTrafficAppBytes();
    }

    public void onSubscriptionChanged() {
        String imsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        if (TextUtils.equals(imsi, this.mImsi)) {
            HwLog.i(TAG, "subscription not changed");
            return;
        }
        HwLog.i(TAG, "subscription changed");
        this.mImsi = imsi;
        onUidListChanged();
    }
}
