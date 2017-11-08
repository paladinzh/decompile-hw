package com.huawei.systemmanager.antivirus;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.backup.CommonPrefBackupProvider.IPreferenceBackup.BasePreferenceBackup;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.Set;

public class AntiVirusBackup extends BasePreferenceBackup {
    private static final String TAG = "AntivirusBackup";

    public ContentValues onQueryPreferences() {
        return getAntiVirusPreference();
    }

    public Set<String> onQueryPreferenceKeys() {
        Set<String> keys = new HashSet();
        keys.add(AntiVirusTools.SCAN_MODE);
        keys.add(AntiVirusTools.IS_AUTO_UPDATE_VIRUS_LIB);
        keys.add(AntiVirusTools.IS_WIFI_ONLY_UPDATE);
        keys.add(AntiVirusTools.CLOUD_SCAN_SWITCH);
        keys.add(AntiVirusTools.GLOBAL_TIMER_REMIND);
        return keys;
    }

    public int onRecoverPreference(String key, String value) {
        return setAntiVirusPreference(key, value);
    }

    private ContentValues getAntiVirusPreference() {
        Context context = GlobalContext.getContext();
        ContentValues values = new ContentValues();
        values.put(AntiVirusTools.SCAN_MODE, String.valueOf(AntiVirusTools.getScanMode(context)));
        values.put(AntiVirusTools.IS_AUTO_UPDATE_VIRUS_LIB, String.valueOf(AntiVirusTools.isAutoUpdate(context)));
        values.put(AntiVirusTools.IS_WIFI_ONLY_UPDATE, String.valueOf(AntiVirusTools.isWiFiOnlyUpdate(context)));
        values.put(AntiVirusTools.CLOUD_SCAN_SWITCH, String.valueOf(AntiVirusTools.isCloudScanSwitchOn(context)));
        values.put(AntiVirusTools.GLOBAL_TIMER_REMIND, String.valueOf(AntiVirusTools.isGlobalTimerSwitchOn(context)));
        return values;
    }

    private int setAntiVirusPreference(String keyString, String valueString) {
        Context context = GlobalContext.getContext();
        HwLog.v(TAG, String.format("setAntiVirusPreference : %1$s = %2$s", new Object[]{keyString, valueString}));
        if (AntiVirusTools.SCAN_MODE.equalsIgnoreCase(keyString)) {
            AntiVirusTools.setScanMode(context, Integer.parseInt(valueString));
            return 1;
        } else if (AntiVirusTools.IS_AUTO_UPDATE_VIRUS_LIB.equalsIgnoreCase(keyString)) {
            AntiVirusTools.setAutoUpdate(context, Boolean.valueOf(Boolean.parseBoolean(valueString)).booleanValue());
            return 1;
        } else if (AntiVirusTools.IS_WIFI_ONLY_UPDATE.equalsIgnoreCase(keyString)) {
            AntiVirusTools.setWifiOnlyUpdate(context, Boolean.valueOf(Boolean.parseBoolean(valueString)).booleanValue());
            return 1;
        } else if (AntiVirusTools.CLOUD_SCAN_SWITCH.equalsIgnoreCase(keyString)) {
            AntiVirusTools.setCloudScan(context, Boolean.valueOf(Boolean.parseBoolean(valueString)).booleanValue());
            return 1;
        } else if (!AntiVirusTools.GLOBAL_TIMER_REMIND.equalsIgnoreCase(keyString)) {
            return 0;
        } else {
            AntiVirusTools.setGlobalTimerRemind(context, Boolean.valueOf(Boolean.parseBoolean(valueString)).booleanValue());
            return 1;
        }
    }
}
