package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.net.TrafficStats;
import com.huawei.systemmanager.util.HwLog;

public class NormalTrafficAnalysis {
    private static final String TAG = "NormalTrafficAnalysis";
    private static NormalTrafficAnalysis sInstance;
    private long mLastAlertByte;

    private NormalTrafficAnalysis() {
    }

    public static synchronized NormalTrafficAnalysis getDefault() {
        NormalTrafficAnalysis normalTrafficAnalysis;
        synchronized (NormalTrafficAnalysis.class) {
            if (sInstance == null) {
                sInstance = new NormalTrafficAnalysis();
            }
            normalTrafficAnalysis = sInstance;
        }
        return normalTrafficAnalysis;
    }

    public void init() {
        updateAlertBytes();
        HwLog.i(TAG, "init normal traffic byte = " + this.mLastAlertByte);
    }

    public long getDeltaTraffic() {
        long alertByte = getAllMobileBytes();
        HwLog.i(TAG, "alert byte = " + alertByte + " last alert byte = " + this.mLastAlertByte);
        if (alertByte == 0) {
            HwLog.e(TAG, "alert byte = 0, so return");
            return 0;
        } else if (this.mLastAlertByte == 0) {
            HwLog.e(TAG, "alert byte != 0, last alert byte = 0, because of stop system manager service");
            this.mLastAlertByte = alertByte;
            return 0;
        } else {
            long deltaByte = alertByte - this.mLastAlertByte;
            if (deltaByte < 0) {
                HwLog.e(TAG, "alert byte < 0, so return");
                return 0;
            }
            this.mLastAlertByte = alertByte;
            return deltaByte;
        }
    }

    public void onSubscriptionChanged() {
        updateAlertBytes();
    }

    private long getAllMobileBytes() {
        long mobileTotal = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
        HwLog.d(TAG, "getAllMobileBytes mobileTotal = " + mobileTotal);
        return mobileTotal;
    }

    private void updateAlertBytes() {
        this.mLastAlertByte = getAllMobileBytes();
    }
}
