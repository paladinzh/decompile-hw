package com.android.server.pfw.policy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.net.HwNetworkPolicyManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.util.Log;
import com.android.server.HwConnectivityService;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HwPFWGoogleServicePolicy extends HwPFWPolicy {
    private static final String GOOGLE_GMS_PAC = "com.google.android.gms";
    private static final String GOOGLE_GSF_PAC = "com.google.android.gsf";
    private static final long MIN_TO_MS = 60000;
    private static final long NORMAL_WAKELOCK_CHECK_TIME = 120000;
    private static final long STATIC_WAKELOCK_CHECK_TIME = 1800000;
    private static final String TAG = "PFW.HwPFWGoogleServicePolicy";
    private static final String US_GOOGLE_URL = "http://www.google.com";
    private static final ArrayList<String> mRestrictNetworkGoogleApps = new ArrayList<String>() {
        {
            add("com.google.android.configupdater");
            add(HwPFWGoogleServicePolicy.GOOGLE_GSF_PAC);
            add("com.google.android.marvin.talkback");
            add("com.google.android.partnersetup");
            add("com.google.android.printservice.recommendation");
            add("com.google.android.syncadapters.contacts");
            add("com.android.pacprocessor");
            add("com.google.android.gms");
        }
    };
    private boolean GAPP_NET_RESTRICT_SWITCHER = SystemProperties.getBoolean("ro.config.hw_gapp_nr", true);
    private long mBootCheckDuration = NORMAL_WAKELOCK_CHECK_TIME;
    private long mCheckDuration = 1800000;
    private boolean mEnabled = true;
    private int mGmsUid = -1;
    private HwNetworkPolicyManager mHwNetworkPolicyManager;
    private boolean mIsChinaMarket;
    private boolean mIsPreventGmsWl;
    private UserManager mUserManager;
    private Handler mWakeLockHandler;
    private Runnable mWakelockMonitor;

    private class WakelockMonitorRemoveUserRunnable implements Runnable {
        private int userId = -1;

        public WakelockMonitorRemoveUserRunnable(int userId) {
            this.userId = userId;
        }

        public void run() {
            HwPFWLogger.d(HwPFWGoogleServicePolicy.TAG, "PreventWake runnable for remove user " + this.userId);
            new Thread() {
                public void run() {
                    if (HwPFWGoogleServicePolicy.this.GAPP_NET_RESTRICT_SWITCHER && HwPFWGoogleServicePolicy.this.mIsChinaMarket && WakelockMonitorRemoveUserRunnable.this.userId != -1) {
                        if (HwPFWGoogleServicePolicy.this.mHwNetworkPolicyManager == null) {
                            Log.i(HwPFWGoogleServicePolicy.TAG, "init HwNetworkPolicyManager instance.");
                            HwPFWGoogleServicePolicy.this.mHwNetworkPolicyManager = HwNetworkPolicyManager.from(HwPFWGoogleServicePolicy.this.mContext);
                        }
                        for (String name : HwPFWGoogleServicePolicy.mRestrictNetworkGoogleApps) {
                            int uid = HwPFWGoogleServicePolicy.this.getGoogleAppUid(name);
                            if (uid != -1) {
                                uid = UserHandle.getUid(WakelockMonitorRemoveUserRunnable.this.userId, uid);
                                HwPFWGoogleServicePolicy.this.mHwNetworkPolicyManager.removeHwUidPolicy(uid, HwPFWGoogleServicePolicy.this.mHwNetworkPolicyManager.getHwUidPolicy(uid));
                            }
                        }
                    }
                }
            }.start();
        }
    }

    public HwPFWGoogleServicePolicy(Context context, HwPFWService service) {
        boolean z = true;
        super(context);
        if (SystemProperties.getInt("ro.config.hw_optb", 0) != 156) {
            z = false;
        }
        this.mIsChinaMarket = z;
        this.mIsPreventGmsWl = this.mIsChinaMarket;
        this.mWakelockMonitor = new Runnable() {
            public void run() {
                HwPFWLogger.d(HwPFWGoogleServicePolicy.TAG, "PreventWake runnable");
                new Thread() {
                    public void run() {
                        HwPFWGoogleServicePolicy.this.mGmsUid = HwPFWGoogleServicePolicy.this.getGmsUid();
                        if (HwPFWGoogleServicePolicy.this.isGoogleConnectOK()) {
                            HwPFWGoogleServicePolicy.this.mIsPreventGmsWl = false;
                            HwPFWLogger.d(HwPFWGoogleServicePolicy.TAG, "PreventWake is invalid!");
                            HwPFWGoogleServicePolicy.this.googleAppsNetworkRestrict(false);
                            return;
                        }
                        HwPFWGoogleServicePolicy.this.mIsPreventGmsWl = true;
                        HwPFWLogger.d(HwPFWGoogleServicePolicy.TAG, "PreventWake change to valid!");
                        HwPFWGoogleServicePolicy.this.googleAppsNetworkRestrict(true);
                    }
                }.start();
                HwPFWLogger.d(HwPFWGoogleServicePolicy.TAG, "mWakelockMonitor mCheckDuration = " + HwPFWGoogleServicePolicy.this.mCheckDuration);
                HwPFWGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(HwPFWGoogleServicePolicy.this.mWakelockMonitor);
                HwPFWGoogleServicePolicy.this.mWakeLockHandler.postDelayed(HwPFWGoogleServicePolicy.this.mWakelockMonitor, HwPFWGoogleServicePolicy.this.mCheckDuration);
            }
        };
        this.mWakeLockHandler = new Handler();
    }

    public void handleBroadcastIntent(Intent intent) {
        if (this.mEnabled) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.USER_ADDED")) {
                    HwPFWLogger.d(TAG, "handleBroadcastIntent mBootCheckDuration = " + this.mBootCheckDuration);
                    this.mWakeLockHandler.removeCallbacks(this.mWakelockMonitor);
                    this.mWakeLockHandler.postDelayed(this.mWakelockMonitor, 0);
                } else if (action.equals(HwConnectivityService.CONNECTIVITY_CHANGE_ACTION)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null && networkInfo.isConnected()) {
                        this.mWakeLockHandler.removeCallbacks(this.mWakelockMonitor);
                        this.mWakeLockHandler.postDelayed(this.mWakelockMonitor, 0);
                    }
                } else if (action.equals("android.intent.action.USER_REMOVED")) {
                    this.mWakeLockHandler.post(new WakelockMonitorRemoveUserRunnable(intent.getIntExtra("android.intent.extra.user_handle", -1)));
                }
            }
        }
    }

    private boolean isGoogleConnectOK() {
        HttpURLConnection httpURLConnection = null;
        int httpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        try {
            try {
                httpURLConnection = (HttpURLConnection) new URL(US_GOOGLE_URL).openConnection();
                httpURLConnection.setConnectTimeout(HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS);
                httpURLConnection.connect();
                httpResponseCode = httpURLConnection.getResponseCode();
                HwPFWLogger.d(TAG, "httpResponseCode = " + httpResponseCode);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Exception e) {
                HwPFWLogger.d(TAG, "failed to connect google.");
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable th) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            if (200 == httpResponseCode) {
                return true;
            }
            return false;
        } catch (MalformedURLException e2) {
            HwPFWLogger.d(TAG, "PreventWake MalformedURLException");
            return false;
        }
    }

    private int getGmsUid() {
        ApplicationInfo ai = null;
        try {
            ai = this.mContext.getPackageManager().getApplicationInfo("com.google.android.gms", 0);
        } catch (Exception e) {
            HwPFWLogger.d(TAG, "failed to get application info");
        }
        if (ai == null) {
            return -1;
        }
        HwPFWLogger.d(TAG, "gmsUid = " + ai.uid);
        return ai.uid;
    }

    public boolean isGmsWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        if (packageName != null && 1 == (65535 & flags) && this.mIsPreventGmsWl) {
            if (packageName.contains("com.google.android.gms") || packageName.contains(GOOGLE_GSF_PAC)) {
                return true;
            }
            if (ws != null) {
                for (int i = 0; i < ws.size(); i++) {
                    if (ws.get(i) == this.mGmsUid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean getPreventGmsVal() {
        return this.mIsPreventGmsWl;
    }

    private void googleAppsNetworkRestrict(boolean isRestrict) {
        if (!this.GAPP_NET_RESTRICT_SWITCHER) {
            Log.i(TAG, "gapp nr fearture is closed, do nothing.");
        } else if (this.mIsChinaMarket) {
            int state;
            Log.i(TAG, "GApp isRestrict = " + isRestrict);
            if (this.mHwNetworkPolicyManager == null) {
                Log.i(TAG, "init HwNetworkPolicyManager instance.");
                this.mHwNetworkPolicyManager = HwNetworkPolicyManager.from(this.mContext);
            }
            if (this.mUserManager == null) {
                this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            }
            if (isRestrict) {
                state = 3;
            } else {
                state = 0;
            }
            List<UserInfo> users = this.mUserManager.getUsers();
            for (String name : mRestrictNetworkGoogleApps) {
                int uid = getGoogleAppUid(name);
                if (uid != -1) {
                    for (UserInfo ui : users) {
                        uid = UserHandle.getUid(ui.id, uid);
                        if ((state & this.mHwNetworkPolicyManager.getHwUidPolicy(uid)) == 0) {
                            this.mHwNetworkPolicyManager.setHwUidPolicy(uid, state);
                        }
                    }
                }
            }
        } else {
            Log.i(TAG, "not china market, do nothing.");
        }
    }

    private int getGoogleAppUid(String name) {
        try {
            ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfo(name, 0);
            if (ai == null) {
                return -1;
            }
            int uid = ai.uid;
            Log.i(TAG, "GApp name = " + name + "  UID = " + uid);
            return uid;
        } catch (Exception e) {
            return -1;
        }
    }
}
