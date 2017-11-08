package com.huawei.watermark.wmdata.wmlistdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WMWatermarkListData {
    private static WMWatermarkListData instance;
    private static SharedPreferences share = null;

    private WMWatermarkListData(Context context) {
        share = context.getSharedPreferences("wmsharepreferencelistdataname", 0);
    }

    public static synchronized WMWatermarkListData getInstance(Context context) {
        WMWatermarkListData wMWatermarkListData;
        synchronized (WMWatermarkListData.class) {
            if (instance == null) {
                instance = new WMWatermarkListData(context);
            }
            wMWatermarkListData = instance;
        }
        return wMWatermarkListData;
    }

    public void clearData() {
        Editor editor = share.edit();
        editor.clear();
        editor.commit();
    }

    public void setIntValue(String key, int value) {
        Editor editor = share.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getIntValue(String key, int defaultvalue) {
        return share.getInt(key, defaultvalue);
    }
}
