package com.android.contacts.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.compatibility.QueryUtil;

public class HiCloudUtil {
    public static final Uri HICLOUD_ACCOUNT_PROVIDER_URI = Uri.parse("content://com.huawei.android.hicloud.loginProvider/login_user");
    public static final Uri HICLOUD_PROVIDER_URI = Uri.parse("content://com.huawei.android.hicloud.syncstate/contacts_autosyncswitch");
    private static String mAccountName = "";
    private static boolean mIsHiCloudAccountHasRead = false;
    private static boolean mIsHiCloudAccountLogOn = false;

    public static boolean isHicloudSyncStateEnabled(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(HICLOUD_PROVIDER_URI, new String[]{"isopen"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("isopen");
                if (-1 != columnIndex) {
                    boolean parseBoolean = Boolean.parseBoolean(cursor.getString(columnIndex));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return parseBoolean;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("HiCloudSyncState", "no permission or other erro", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static int getHicloudAccountState(Context context) {
        if (!QueryUtil.isHAPProviderInstalled()) {
            return 0;
        }
        Cursor cursor = null;
        int hiCloudState = 0;
        try {
            cursor = context.getContentResolver().query(HICLOUD_ACCOUNT_PROVIDER_URI, new String[]{"accountName"}, null, null, null);
            if (cursor == null) {
                mAccountName = "";
                hiCloudState = 0;
                mIsHiCloudAccountLogOn = false;
            } else if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("accountName");
                if (-1 != columnIndex) {
                    mAccountName = cursor.getString(columnIndex);
                }
                if (TextUtils.isEmpty(mAccountName)) {
                    mAccountName = "";
                    mIsHiCloudAccountLogOn = false;
                    hiCloudState = 2;
                    HwLog.e("HiCloudSyncState", "HiCloud is log on, but accountName is empty");
                } else {
                    mIsHiCloudAccountLogOn = true;
                    hiCloudState = 1;
                }
            } else {
                mAccountName = "";
                mIsHiCloudAccountLogOn = false;
                hiCloudState = 2;
            }
            if (cursor != null) {
                mIsHiCloudAccountHasRead = true;
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("HiCloudSyncState", "no permission or other erro", e);
            if (cursor != null) {
                mIsHiCloudAccountHasRead = true;
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                mIsHiCloudAccountHasRead = true;
                cursor.close();
            }
        }
        return hiCloudState;
    }

    public static void setHicloudAccount(String accountName) {
        mAccountName = accountName;
    }

    public static String getHiCloudAccountName() {
        return mAccountName;
    }

    public static boolean isHiCloudAccountLogOn() {
        return mIsHiCloudAccountLogOn;
    }

    public static void setHuaWeiCloudAccountLogOn(boolean isHuaweiCloundLogOn) {
        mIsHiCloudAccountLogOn = isHuaweiCloundLogOn;
    }

    public static boolean isHiCloudAccountHasRead() {
        return mIsHiCloudAccountHasRead;
    }
}
