package com.huawei.systemmanager.spacecleanner.setting;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.optimize.monitor.MemCPUMonitor;
import com.huawei.systemmanager.util.HwLog;

public class SpaceScheduleService extends IntentService {
    public static final String ACTION_AUTO_CHECK_CACHE = "com.huawei.systemmanager.spacecleanner.checkcache";
    public static final String ACTION_AUTO_CLEAN_CACHE = "com.huawei.systemmanager.spacecleanner.cacheclean";
    public static final String ACTION_AUTO_UPDATE_SPACE_LIB = "com.huawei.systemmanager.spacecleanner.autoupdate";
    public static final String ACTION_CLEAN_TIMING_NOTIFY = "com.huawei.systemmanager.spacecleanner.cleantimingnotify";
    public static final String ACTION_PHONE_SLOW_NOTIFY = "com.huawei.systemmanager.spacecleanner.phoneslow";
    public static final String TAG = SpaceScheduleService.class.getSimpleName();

    public SpaceScheduleService() {
        super(TAG);
        MemCPUMonitor.getInstance(GlobalContext.getContext());
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            HwLog.d(TAG, "SpaceScheduleService action is " + intent.getAction());
            if (TextUtils.equals(ACTION_AUTO_UPDATE_SPACE_LIB, intent.getAction())) {
                UpdateSetting autoUpdateSetting = SpaceSettingPreference.getDefault().getUpdateSetting();
                if (!intent.getBooleanExtra("android.net.conn.CONNECTIVITY_CHANGE", false) || !autoUpdateSetting.isSeccussUpdateInTime()) {
                    boolean isWifiOnlyUpdate = SpaceSettingPreference.getDefault().getOnlyWifiUpdateSetting().isSwitchOn();
                    NetworkInfo networkInfo = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isAvailable()) {
                        HwLog.i(TAG, "space lib will not update because of the network is not available");
                    } else if (!isWifiOnlyUpdate) {
                        Utility.initSDK(GlobalContext.getContext());
                        HwLog.i(TAG, "space lib update in normal mode");
                        autoUpdateSetting.doAutoUpdate();
                    } else if (networkInfo.getType() == 1) {
                        Utility.initSDK(GlobalContext.getContext());
                        HwLog.d(TAG, "space lib update in WIFI only mode");
                        autoUpdateSetting.doAutoUpdate();
                    } else {
                        HwLog.i(TAG, "space lib will not update because of the network type is not WIFI");
                    }
                }
            } else if (TextUtils.equals(ACTION_AUTO_CLEAN_CACHE, intent.getAction())) {
                SpaceSettingPreference.getDefault().getCacheCleanSetting().doAction();
            } else if (TextUtils.equals(ACTION_AUTO_CHECK_CACHE, intent.getAction())) {
                SpaceSettingPreference.getDefault().getCacheCleanSetting().doCheck();
            } else if (TextUtils.equals(ACTION_PHONE_SLOW_NOTIFY, intent.getAction())) {
                SpaceSettingPreference.getDefault().getPhoneSlowSetting().doAction();
            }
        }
    }
}
