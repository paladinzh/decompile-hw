package com.huawei.systemmanager.rainbow.client.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.util.HwLog;

public class LocalSharedPrefrenceHelper {
    private static final String TAG = "LocalSharedPrefrenceService";
    private Context mContext;
    private SharedPreferences sp = null;

    public LocalSharedPrefrenceHelper(Context context) {
        this.mContext = context.getApplicationContext();
        this.sp = this.mContext.getSharedPreferences(CloudSpfKeys.FILE_NAME, 0);
    }

    public int getInt(String key, int defaultValue) {
        int value = 0;
        try {
            value = this.sp.getInt(key, defaultValue);
        } catch (Exception e) {
            HwLog.e(TAG, e.toString());
        }
        return value;
    }

    public String getString(String key, String defValue) {
        try {
            return this.sp.getString(key, defValue);
        } catch (Exception e) {
            HwLog.e(TAG, e.toString());
            return "";
        }
    }

    public long getLong(String key, long defaultValue) {
        long value = 0;
        try {
            value = this.sp.getLong(key, defaultValue);
        } catch (Exception e) {
            HwLog.e(TAG, e.toString());
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean value = false;
        try {
            value = this.sp.getBoolean(key, defaultValue);
        } catch (Exception e) {
            HwLog.e(TAG, e.toString());
        }
        return value;
    }

    public boolean putInt(String key, int value) {
        try {
            Editor editor = this.sp.edit();
            editor.putInt(key, value);
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean putString(String key, String value) {
        try {
            Editor editor = this.sp.edit();
            editor.putString(key, value);
            return editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
            return false;
        }
    }

    public boolean putLong(String key, long value) {
        try {
            Editor editor = this.sp.edit();
            editor.putLong(key, value);
            return editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
            return false;
        }
    }

    public boolean putBoolean(String key, boolean value) {
        try {
            Editor editor = this.sp.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
            return false;
        }
    }
}
