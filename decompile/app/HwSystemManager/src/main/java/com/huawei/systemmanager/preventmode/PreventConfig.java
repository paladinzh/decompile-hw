package com.huawei.systemmanager.preventmode;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.preventmode.util.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import com.huawei.systemmanager.util.phonematch.PhoneMatchInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PreventConfig {
    private static final String TAG = "PreventConfig";
    private Context mContext = null;

    public PreventConfig(Context context) {
        this.mContext = context;
    }

    public Cursor queryPreventWhiteListDB() {
        return this.mContext.getContentResolver().query(Const.PREVENT_WHITE_LIST, null, null, null, null);
    }

    public Cursor queryPreVentWhiteNumber(String shortNumber, boolean phoneExactMatch) {
        String selection;
        String[] selectionArgs;
        if (phoneExactMatch) {
            selection = "Phone_number=?";
            selectionArgs = new String[]{shortNumber};
        } else {
            selection = "Phone_number like ?";
            selectionArgs = new String[]{"%" + shortNumber};
        }
        return this.mContext.getContentResolver().query(Const.PREVENT_WHITE_LIST, new String[]{Const.PREVENT_WHITE_LIST_NUMBER}, selection, selectionArgs, null);
    }

    public int queryWhiteListCount() {
        Cursor cursor = this.mContext.getContentResolver().query(Const.PREVENT_WHITE_LIST, new String[]{"count(*)"}, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        }
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    public String[] queryAllPhoneNo() {
        String[] projection = new String[]{Const.PREVENT_WHITE_LIST_NUMBER};
        ArrayList<String> phoneNoList = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(Const.PREVENT_WHITE_LIST, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        while (cursor.moveToNext()) {
            phoneNoList.add(cursor.getString(0));
        }
        cursor.close();
        return (String[]) phoneNoList.toArray(new String[phoneNoList.size()]);
    }

    public boolean isWhiteNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.equals("")) {
            return false;
        }
        return foundWhiteNumber(PhoneMatch.getPhoneNumberMatchInfo(Utility.formatPhoneNumber(phoneNumber)));
    }

    private boolean foundWhiteNumber(PhoneMatchInfo matchInfo) {
        Cursor cursor = queryPreVentWhiteNumber(matchInfo.getPhoneNumber(), matchInfo.isExactMatch());
        boolean isWhite = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isWhite = true;
            }
            cursor.close();
        }
        return isWhite;
    }

    public long insertPreventWhiteListDB(ContentValues values) {
        Uri retUri = this.mContext.getContentResolver().insert(Const.PREVENT_WHITE_LIST, values);
        if (retUri != null && Const.PREVENT_WHITE_LIST != retUri) {
            return ContentUris.parseId(retUri);
        }
        HwLog.w(TAG, "insertPreventWhiteListDB: Failed");
        return -1;
    }

    public void bulkInsertPreventWhiteListDB(ContentValues[] values) {
        this.mContext.getContentResolver().bulkInsert(Const.PREVENT_WHITE_LIST, values);
    }

    public HashMap<String, Object> getWhiteListNumber(Cursor cursor) {
        HashMap<String, Object> whiteMapNumber = new HashMap();
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                whiteMapNumber.put(Const.PREVENT_WHITE_LIST_NUMBER, Integer.valueOf(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NUMBER)));
                cursor.moveToNext();
            }
        }
        return whiteMapNumber;
    }

    public void deletePreventModeWhiteListDB(String where) {
        this.mContext.getContentResolver().delete(Const.PREVENT_WHITE_LIST, "Phone_number = ? ", new String[]{where});
    }

    public static String getShortNumber(String phoneNumber) {
        phoneNumber = Utility.reserveData(phoneNumber);
        String shortNumber = phoneNumber;
        char[] phoneNumberChar = phoneNumber.toCharArray();
        if (phoneNumberChar.length >= 7) {
            return phoneNumber.substring(phoneNumberChar.length - 7);
        }
        return shortNumber;
    }

    public int deleteAllPreventlist() {
        return ProviderUtils.deleteAll(this.mContext, Const.PREVENT_WHITE_LIST);
    }

    public int updateWhiteList(List<ContactInfo> values) {
        int nUpdateCount = 0;
        for (ContactInfo info : values) {
            ContentValues updateValue = new ContentValues();
            updateValue.put(Const.PREVENT_WHITE_LIST_NAME, info.getmName());
            try {
                nUpdateCount += this.mContext.getContentResolver().update(Const.PREVENT_WHITE_LIST, updateValue, "Phone_number=?", new String[]{info.getmPhone()});
            } catch (Exception e) {
                HwLog.e(TAG, "updateWhiteList ,Exception, Uri = " + e.toString());
                return -1;
            }
        }
        return nUpdateCount;
    }
}
