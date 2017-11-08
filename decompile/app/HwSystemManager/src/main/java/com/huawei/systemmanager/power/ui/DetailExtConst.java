package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.os.Bundle;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.util.AppRangeWrapper;
import com.huawei.systemmanager.power.util.L2MAdapter;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Map.Entry;

public class DetailExtConst {
    public static final int ENTER_INTO_DETAIL_FROM_BACKGOUND = 1;
    public static final int ENTER_INTO_DETAIL_FROM_CONSUMELEVEL = 2;
    public static final String ENTER_INTO_DETAIL_TYPE_KEY = "enter_into_detail_type";
    public static final String SOFTWARE_ANDROID_OS = "android os";
    public static final String SOFTWARE_MEDIA = "media";
    private static final String TAG = DetailExtConst.class.getSimpleName();

    interface SoftDetailKey {
        public static final String EXT_SOFT_DETAIL_CPU_FRONT_TIME = "ext_cpu_front_time";
        public static final String EXT_SOFT_DETAIL_CPU_TIME = "ext_cpu_time";
        public static final String EXT_SOFT_DETAIL_DATA_RECV = "ext_data_recv_bytes";
        public static final String EXT_SOFT_DETAIL_DATA_SEND = "ext_data_send_bytes";
        public static final String EXT_SOFT_DETAIL_GPS_TIME = "ext_gps_time";
        public static final String EXT_SOFT_DETAIL_PACKAGE_NAME = "ext_package_name";
        public static final String EXT_SOFT_DETAIL_POWER_VALUE = "ext_power_value";
        public static final String EXT_SOFT_DETAIL_PREVENT_SLEEP_TIME = "ext_prevent_sleep_time";
        public static final String EXT_SOFT_DETAIL_SHOW_BG_VIEW = "ext_show_bg_view";
        public static final String EXT_SOFT_DETAIL_UID = "ext_uid";
        public static final String EXT_SOFT_DETAIL_WIFI_RUNNING_TIME = "ext_wifi_running_time";
        public static final String EXT_SOFT_DETAIL_WLAN_RECV = "ext_wlan_recv_bytes";
        public static final String EXT_SOFT_DETAIL_WLAN_SEND = "ext_wlan_send_bytes";
        public static final String FROM_SETTINGS_PACKAGE_NAME = "package";
        public static final String FROM_SETTINGS_UID = "uid";
    }

    public static Bundle sipperToBundle(String packageName, int uid, BatterySipper sipper, boolean showBgView) {
        long wakeLockTime;
        long j = 0;
        Bundle bundle = new Bundle();
        if (sipper == null) {
            HwLog.w(TAG, "sipperToBundle null input sipper, maybe USB is plugged");
        }
        bundle.putInt(SoftDetailKey.EXT_SOFT_DETAIL_UID, uid);
        bundle.putString(SoftDetailKey.EXT_SOFT_DETAIL_PACKAGE_NAME, packageName);
        bundle.putDouble(SoftDetailKey.EXT_SOFT_DETAIL_POWER_VALUE, sipper != null ? L2MAdapter.value(sipper) : 0.0d);
        String str = SoftDetailKey.EXT_SOFT_DETAIL_PREVENT_SLEEP_TIME;
        if (sipper != null) {
            wakeLockTime = L2MAdapter.wakeLockTime(sipper);
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_CPU_TIME;
        if (sipper != null) {
            wakeLockTime = L2MAdapter.cpuTime(sipper);
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_CPU_FRONT_TIME;
        if (sipper != null) {
            wakeLockTime = L2MAdapter.cpuFgTime(sipper);
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_GPS_TIME;
        if (sipper != null) {
            wakeLockTime = L2MAdapter.gpsTime(sipper);
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_WIFI_RUNNING_TIME;
        if (sipper != null) {
            wakeLockTime = L2MAdapter.wifiRunningTime(sipper);
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_DATA_SEND;
        if (sipper != null) {
            wakeLockTime = sipper.mobileTxBytes;
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_DATA_RECV;
        if (sipper != null) {
            wakeLockTime = sipper.mobileRxBytes;
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_WLAN_SEND;
        if (sipper != null) {
            wakeLockTime = sipper.wifiTxBytes;
        } else {
            wakeLockTime = 0;
        }
        bundle.putLong(str, wakeLockTime);
        str = SoftDetailKey.EXT_SOFT_DETAIL_WLAN_RECV;
        if (sipper != null) {
            j = sipper.wifiRxBytes;
        }
        bundle.putLong(str, j);
        bundle.putBoolean(SoftDetailKey.EXT_SOFT_DETAIL_SHOW_BG_VIEW, showBgView);
        return bundle;
    }

    public static Bundle mapToBundle(Map<String, Double> procPowermap) {
        Bundle bundle = new Bundle();
        for (Entry<String, Double> entry : procPowermap.entrySet()) {
            bundle.putDouble((String) entry.getKey(), ((Double) entry.getValue()).doubleValue());
        }
        return bundle;
    }

    public static Bundle getConsumeExtInfoByPackageName(Context mAppContext, String pkgName, int uid) {
        Bundle mBundle = null;
        PowerStatsHelper mPowerStatsHelper = PowerStatsHelper.newInstance(mAppContext, true);
        try {
            for (BatterySipper sipper : mPowerStatsHelper.computeSwAndHwConsumption(mAppContext, true)) {
                if (DrainType.APP == sipper.drainType && uid == sipper.getUid()) {
                    int uidType = SysCoreUtils.getUidType(uid);
                    HwLog.i(TAG, "Uid= " + uid + " , PkgName= " + pkgName + " is EXIST in the  mSoftwareList");
                    boolean isExist = false;
                    for (Integer intValue : AppRangeWrapper.getAppThirdUidSet(mAppContext)) {
                        if (intValue.intValue() == uid) {
                            isExist = true;
                            break;
                        }
                    }
                    mBundle = sipperToBundle(pkgName, uid, sipper, isExist);
                    if (uidType == 1) {
                        mBundle.putAll(mapToBundle(mPowerStatsHelper.getPackageNameAndPower(mAppContext, sipper.uidObj)));
                    }
                    HwLog.i(TAG, " mBundle= " + mBundle.toString());
                    return mBundle;
                }
            }
        } catch (PowerStatsException ex) {
            HwLog.e(TAG, "doInBackground catch PowerStatsException: " + ex.getMessage());
            ex.printStackTrace();
        }
        return mBundle;
    }
}
