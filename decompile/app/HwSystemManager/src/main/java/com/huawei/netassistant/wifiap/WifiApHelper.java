package com.huawei.netassistant.wifiap;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import java.util.ArrayList;

public class WifiApHelper {
    private static final String ACTION_WIFI_AP_SETTINGS = "android.settings.WIFI_AP_SETTINGS";
    private static final int LIMIT_REACH_NOTIFICATION_ID = 2131231501;
    private static final int NETWORK_CLASS_2G = 2;
    private static final int NETWORK_CLASS_3G = 3;
    private static final int NETWORK_CLASS_4G = 4;
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    private static final long NETWORK_SPEED_2G = 102400;
    private static final long NETWORK_SPEED_3G = 10485760;
    private static final long NETWORK_SPEED_4G = 104857600;
    public static final int TETHER_STATE_ERROR = 0;
    public static final int TETHER_STATE_SUCCEED = 1;
    public static final int TETHER_STATE_UNKNOW = 2;
    private static WifiApHelper mInstance = null;
    private static boolean mIsApEnabled = false;
    private ConnectivityManager mCm = ((ConnectivityManager) this.mContext.getSystemService("connectivity"));
    private Context mContext = GlobalContext.getContext();
    private String[] mWifiRegexs = this.mCm.getTetherableWifiRegexs();

    private WifiApHelper() {
    }

    public static synchronized WifiApHelper getInstance() {
        WifiApHelper wifiApHelper;
        synchronized (WifiApHelper.class) {
            if (mInstance == null) {
                mInstance = new WifiApHelper();
            }
            wifiApHelper = mInstance;
        }
        return wifiApHelper;
    }

    public static synchronized void destroyInstance() {
        synchronized (WifiApHelper.class) {
            if (mInstance == null) {
                return;
            }
            mInstance = null;
        }
    }

    public static synchronized void setApState(boolean isEnabled) {
        synchronized (WifiApHelper.class) {
            mIsApEnabled = isEnabled;
        }
    }

    public static synchronized boolean getApState() {
        boolean z;
        synchronized (WifiApHelper.class) {
            z = mIsApEnabled;
        }
        return z;
    }

    public int parseTetherState(Intent intentTetherState) {
        if (!"android.net.conn.TETHER_STATE_CHANGED".equals(intentTetherState.getAction())) {
            return 2;
        }
        ArrayList<String> active = intentTetherState.getStringArrayListExtra("activeArray");
        if (active != null && isTetherdSuccess(active.toArray())) {
            return 1;
        }
        ArrayList<String> errored = intentTetherState.getStringArrayListExtra("erroredArray");
        if (errored == null || !isTetherdError(errored.toArray())) {
            return 2;
        }
        return 0;
    }

    private boolean isTetherdSuccess(Object[] tethered) {
        if (tethered == null) {
            return false;
        }
        for (String s : tethered) {
            for (String regex : this.mWifiRegexs) {
                if (s.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTetherdError(Object[] errored) {
        if (errored == null) {
            return false;
        }
        for (String s : errored) {
            for (String regex : this.mWifiRegexs) {
                if (s.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                return 2;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return 3;
            case 13:
                return 4;
            default:
                return 0;
        }
    }

    public static long getMaxNetworkSpeed(int networkClass) {
        switch (networkClass) {
            case 0:
                return 102400;
            case 2:
                return 102400;
            case 3:
                return 10485760;
            case 4:
                return 104857600;
            default:
                return 102400;
        }
    }

    public static void sendReachLimitNotification(Context context, String limitSize) {
        Builder nBuilder = new Builder(context);
        String notifyTicker = context.getString(R.string.wifihotspot_disabled_notification_title);
        String notifyTitle = notifyTicker;
        String notifyContent = context.getString(R.string.wifihotspot_disabled_notification_content, new Object[]{limitSize});
        Intent intent = new Intent(ACTION_WIFI_AP_SETTINGS);
        intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 134217728);
        nBuilder.setSmallIcon(R.drawable.ic_connection_notify_hotspot_unusable);
        nBuilder.setTicker(notifyTicker);
        nBuilder.setContentTitle(notifyTicker);
        nBuilder.setContentText(notifyContent);
        nBuilder.setContentIntent(pendingIntent);
        nBuilder.setAutoCancel(true);
        Notification notification = nBuilder.build();
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        nm.cancel(R.string.wifihotspot_disabled_notification_title);
        nm.notify(R.string.wifihotspot_disabled_notification_title, notification);
    }

    public static void cancelReachLimitNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(R.string.wifihotspot_disabled_notification_title);
    }
}
