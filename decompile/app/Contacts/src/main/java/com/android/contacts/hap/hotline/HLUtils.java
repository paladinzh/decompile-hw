package com.android.contacts.hap.hotline;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;

public class HLUtils {
    private static final String TAG = HLUtils.class.getSimpleName();
    public static final boolean isShowHotNumberOnTop = SystemProperties.getBoolean("ro.config.hw_hot_number_top", false);
    private static Uri mHotNumberKey = null;

    private static void setHotNumberKey(Uri lookupKeyUri) {
        mHotNumberKey = lookupKeyUri;
    }

    public static Uri getHotNumberKey() {
        return mHotNumberKey;
    }

    public static void initPredefineContactLookupUri(Context mContext) {
        if (mContext != null) {
            Uri uri = Contacts.CONTENT_URI.buildUpon().build();
            StringBuilder selection = new StringBuilder();
            selection.append("is_care=1");
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"_id", "lookup"}, selection.toString(), null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        setHotNumberKey(Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1)));
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                } catch (Exception e) {
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExistHotNumber(Context mContext) {
        if (mContext == null) {
            return false;
        }
        Uri uri = Contacts.CONTENT_URI.buildUpon().build();
        StringBuilder selection = new StringBuilder();
        selection.append("is_care=1");
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"_id"}, selection.toString(), null, null);
        boolean isExistHotNum = false;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (cursor.getCount() > 0) {
                        isExistHotNum = true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return isExistHotNum;
                }
            } catch (Exception e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        setHotNumberKey(null);
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }
}
