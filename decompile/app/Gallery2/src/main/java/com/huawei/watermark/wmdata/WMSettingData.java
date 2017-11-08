package com.huawei.watermark.wmdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WMSettingData {
    private static WMSettingData instance;
    private static SharedPreferences share = null;

    private WMSettingData(Context context) {
        share = context.getSharedPreferences("wmsharepreferencesettingdataname", 0);
    }

    public static synchronized WMSettingData getInstance(Context context) {
        WMSettingData wMSettingData;
        synchronized (WMSettingData.class) {
            if (instance == null) {
                instance = new WMSettingData(context);
            }
            wMSettingData = instance;
        }
        return wMSettingData;
    }

    public void clearData() {
        Editor editor = share.edit();
        editor.clear();
        editor.commit();
    }

    public void setBooleanValue(String key, boolean value) {
        Editor editor = share.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBooleanValue(String key, boolean defaultvalue) {
        return share.getBoolean(key, defaultvalue);
    }
}
