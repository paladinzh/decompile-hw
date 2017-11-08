package com.huawei.permissionmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseIntArray;

public class RecommendDBHelper {
    private static final Uri COMMON_TABLE_NAME_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/common");
    private static final int RECOMMEND_PACKAGEINSTALL_CLOSE = 0;
    private static final int RECOMMEND_PACKAGEINSTALL_KEY = 20141223;
    private static final int RECOMMEND_PACKAGEINSTALL_OPEN = 1;
    private static final int RECOMMEND_TIP_KEY = 20141219;
    private static final int RECOMMEND_TIP_STATE_CLOSE = 0;
    private static final int RECOMMEND_TIP_STATE_OPEN = 1;
    private static final int SEND_GROUP_SMS_KEY = 20150126;
    private static final int SEND_GROUP_SMS_STATE_CLOSE = 0;
    private static final int SEND_GROUP_SMS_STATE_OPEN = 1;
    private static RecommendDBHelper sInstance = null;
    private static Object syncObj = new Object();
    private static Object syncPackageInstallLock = new Object();
    private static Object syncSendGroupSmsLock = new Object();
    private static Object syncTipLock = new Object();
    private Context mContext = null;
    private boolean mFirstInit = true;
    private int mRecommendPackageInstall = 1;
    private int mRecommendTipStatus = 0;
    private int mSendGroupSms = 1;

    private RecommendDBHelper(Context context) {
        this.mContext = context;
        initSwitchStatus();
    }

    private void initSwitchStatus() {
        SparseIntArray intMap = new SparseIntArray();
        Cursor cursor = this.mContext.getContentResolver().query(COMMON_TABLE_NAME_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int KeyIndex = cursor.getColumnIndex("key");
                int valueIndex = cursor.getColumnIndex(DBHelper.VALUE);
                while (cursor.moveToNext()) {
                    intMap.put(cursor.getInt(KeyIndex), cursor.getInt(valueIndex));
                }
            }
            cursor.close();
        }
        synchronized (syncTipLock) {
            this.mRecommendTipStatus = intMap.get(RECOMMEND_TIP_KEY, 0);
        }
        synchronized (syncPackageInstallLock) {
            this.mRecommendPackageInstall = intMap.get(RECOMMEND_PACKAGEINSTALL_KEY, 1);
        }
        synchronized (syncSendGroupSmsLock) {
            this.mSendGroupSms = intMap.get(SEND_GROUP_SMS_KEY, 1);
        }
    }

    public void refresh() {
        initSwitchStatus();
    }

    public static RecommendDBHelper getInstance(Context context) {
        RecommendDBHelper recommendDBHelper;
        synchronized (syncObj) {
            if (sInstance == null) {
                sInstance = new RecommendDBHelper(context.getApplicationContext());
            }
            recommendDBHelper = sInstance;
        }
        return recommendDBHelper;
    }

    public boolean getRecommendTipSwitchOpenStatus() {
        boolean z = true;
        synchronized (syncTipLock) {
            if (1 != this.mRecommendTipStatus) {
                z = false;
            }
        }
        return z;
    }

    public void setRecommendTipSwitch() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", Integer.valueOf(RECOMMEND_TIP_KEY));
        contentValues.put(DBHelper.VALUE, Integer.valueOf(1));
        Cursor cursor = this.mContext.getContentResolver().query(COMMON_TABLE_NAME_URI, null, "key = 1", null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                this.mContext.getContentResolver().update(COMMON_TABLE_NAME_URI, contentValues, "key = 1", null);
            } else {
                this.mContext.getContentResolver().insert(COMMON_TABLE_NAME_URI, contentValues);
            }
            cursor.close();
        }
        synchronized (syncTipLock) {
            this.mRecommendTipStatus = 1;
        }
    }

    public boolean getRecommendPackageInstallSwitchStatus() {
        boolean z = true;
        synchronized (syncPackageInstallLock) {
            if (1 != this.mRecommendPackageInstall) {
                z = false;
            }
        }
        return z;
    }

    public boolean getRecommendPackageInstallStatusForServiceProcess() {
        boolean z = true;
        if (this.mFirstInit) {
            this.mFirstInit = false;
        } else {
            initSwitchStatus();
        }
        synchronized (syncPackageInstallLock) {
            if (1 != this.mRecommendPackageInstall) {
                z = false;
            }
        }
        return z;
    }

    public void setRecommendPackageInstallSwitch(boolean openStatus) {
        int enableStatus = openStatus ? 1 : 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", Integer.valueOf(RECOMMEND_PACKAGEINSTALL_KEY));
        contentValues.put(DBHelper.VALUE, Integer.valueOf(enableStatus));
        Cursor cursor = this.mContext.getContentResolver().query(COMMON_TABLE_NAME_URI, null, "key = 20141223", null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                this.mContext.getContentResolver().update(COMMON_TABLE_NAME_URI, contentValues, "key = 20141223", null);
            } else {
                this.mContext.getContentResolver().insert(COMMON_TABLE_NAME_URI, contentValues);
            }
            cursor.close();
        }
        synchronized (syncPackageInstallLock) {
            this.mRecommendPackageInstall = enableStatus;
        }
    }

    public boolean getSendGroupSmsSwitchStatus() {
        boolean z = true;
        synchronized (syncSendGroupSmsLock) {
            if (1 != this.mSendGroupSms) {
                z = false;
            }
        }
        return z;
    }

    public boolean getSendGroupSmsStatusForServiceProcess() {
        boolean z = true;
        if (this.mFirstInit) {
            this.mFirstInit = false;
        } else {
            initSwitchStatus();
        }
        synchronized (syncSendGroupSmsLock) {
            if (1 != this.mSendGroupSms) {
                z = false;
            }
        }
        return z;
    }

    public void setSendGroupSmsSwitch(boolean openStatus) {
        int enableStatus = openStatus ? 1 : 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", Integer.valueOf(SEND_GROUP_SMS_KEY));
        contentValues.put(DBHelper.VALUE, Integer.valueOf(enableStatus));
        Cursor cursor = this.mContext.getContentResolver().query(COMMON_TABLE_NAME_URI, null, "key = 20150126", null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                this.mContext.getContentResolver().update(COMMON_TABLE_NAME_URI, contentValues, "key = 20150126", null);
            } else {
                this.mContext.getContentResolver().insert(COMMON_TABLE_NAME_URI, contentValues);
            }
            cursor.close();
        }
        synchronized (syncSendGroupSmsLock) {
            this.mSendGroupSms = enableStatus;
        }
    }
}
