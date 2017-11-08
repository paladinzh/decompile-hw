package com.huawei.watermark.wmdata.wmlogicdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WMLogicData {
    private static WMLogicData instance;
    private static SharedPreferences share = null;

    private WMLogicData(Context context) {
        share = context.getSharedPreferences("wmsharepreferencelogicdataname", 0);
    }

    public static synchronized WMLogicData getInstance(Context context) {
        WMLogicData wMLogicData;
        synchronized (WMLogicData.class) {
            if (instance == null) {
                instance = new WMLogicData(context);
            }
            wMLogicData = instance;
        }
        return wMLogicData;
    }

    public synchronized void clearData() {
        if (share != null) {
            Editor editor = share.edit();
            editor.clear();
            editor.commit();
        }
    }

    public synchronized void setEditTextWithKeyname(String viewIdStr, String editValue) {
        setStringValue(viewIdStr, editValue);
    }

    public synchronized String getEditTextWithKeyname(String viewIdStr) {
        return getStringValue(viewIdStr, null);
    }

    public synchronized void setLocationTextWithKeyname(String viewIdStr, String locationTextValue) {
        setStringValue(viewIdStr, locationTextValue);
    }

    public synchronized String getLocationTextWithKeyname(String viewIdStr) {
        return getStringValue(viewIdStr, null);
    }

    public synchronized void setAltitudeTextWithKeyname(String viewIdStr, String locationTextValue) {
        setStringValue(viewIdStr, locationTextValue);
    }

    public synchronized String getAltitudeTextWithKeyname(String viewIdStr) {
        return getStringValue(viewIdStr, null);
    }

    private synchronized void setStringValue(String key, String value) {
        if (share != null) {
            Editor editor = share.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    private synchronized String getStringValue(String key, String defaultValue) {
        if (share == null) {
            return null;
        }
        return share.getString(key, defaultValue);
    }

    public synchronized String getHealthTextWithKeyname(String health_tag) {
        return getStringValue(health_tag, null);
    }

    public synchronized void setHealthTextWithKeyname(String health_tag, String text) {
        setStringValue(health_tag, text);
    }
}
