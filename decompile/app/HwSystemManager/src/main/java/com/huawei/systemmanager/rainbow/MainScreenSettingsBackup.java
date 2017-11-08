package com.huawei.systemmanager.rainbow;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.systemmanager.backup.CommonPrefBackupProvider.IPreferenceBackup.BasePreferenceBackup;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.Set;

public class MainScreenSettingsBackup extends BasePreferenceBackup {
    private static final String TAG = "MainScreenSettingsBackup";

    public ContentValues onQueryPreferences() {
        return getPreference();
    }

    public Set<String> onQueryPreferenceKeys() {
        Set<String> keys = new HashSet();
        keys.add(CloudSpfKeys.SYSTEM_MANAGER_CLOUD);
        return keys;
    }

    public int onRecoverPreference(String key, String value) {
        return setPreference(key, value);
    }

    private ContentValues getPreference() {
        Context context = GlobalContext.getContext();
        ContentValues values = new ContentValues();
        values.put(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, CloudClientOperation.getSystemManageCloudsStatus(context) ? ClientConstant.SYSTEM_CLOUD_OPEN : ClientConstant.SYSTEM_CLOUD_CLOSE);
        return values;
    }

    private int setPreference(String keyString, String valueString) {
        Context context = GlobalContext.getContext();
        HwLog.i(TAG, String.format("setPreference : %1$s = %2$s", new Object[]{keyString, valueString}));
        if (!CloudSpfKeys.SYSTEM_MANAGER_CLOUD.equalsIgnoreCase(keyString)) {
            return 0;
        }
        if (ClientConstant.SYSTEM_CLOUD_OPEN.equalsIgnoreCase(valueString)) {
            CloudClientOperation.openSystemManageClouds(context);
        } else {
            CloudClientOperation.closeSystemManageClouds(context);
        }
        return 1;
    }
}
