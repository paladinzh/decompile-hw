package com.huawei.harassmentinterception.blackwhitelist;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BlockedNumberContract;
import android.provider.BlockedNumberContract.BlockedNumbers;
import com.google.android.collect.Lists;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import com.huawei.systemmanager.util.phonematch.PhoneMatchInfo;
import java.util.List;

public class GoogleBlackListContract {
    public static final Uri BLACK_LIST_URI = BlockedNumbers.CONTENT_URI;
    private static final String TAG = "GoogleBlackListContract";

    public static String getBlockedNumberById(Uri uri) {
        String result = null;
        if (ContentUris.parseId(uri) < 0) {
            HwLog.e(TAG, "uri does not contain id");
            return null;
        }
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(uri, new String[]{"original_number"}, null, null, null);
        while (cursor.moveToNext()) {
            try {
                result = cursor.getString(0);
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public static List<String> getBlockedNumbers() {
        List<String> blockList = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = GlobalContext.getContext().getContentResolver().query(BLACK_LIST_URI, new String[]{"original_number"}, null, null, null);
            while (cursor.moveToNext()) {
                blockList.add(cursor.getString(0));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return blockList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return blockList;
    }

    public static int deleteBlockedNumber(String blackNumber) {
        PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(blackNumber);
        return GlobalContext.getContext().getContentResolver().delete(BLACK_LIST_URI, phoneMatchInfo.getSqlSelectionStatement("original_number"), phoneMatchInfo.getSqlSelectionArgs());
    }

    public static boolean addBlockedNumber(String number) {
        try {
            if (BlockedNumberContract.isBlocked(GlobalContext.getContext(), number)) {
                HwLog.e(TAG, "number has been added");
                return false;
            }
            ContentValues values = new ContentValues();
            values.put("original_number", number);
            GlobalContext.getContext().getContentResolver().insert(BLACK_LIST_URI, values);
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "query google blockednumber fail");
            return false;
        }
    }

    public static int deleteAllBlockedNumber() {
        return GlobalContext.getContext().getContentResolver().delete(BlockedNumbers.CONTENT_URI, null, null);
    }

    public static boolean isGoogleBlockNumberType(int type) {
        return type == 0;
    }

    public static boolean isBlackListAdded(Uri uri) {
        return ContentUris.parseId(uri) >= 0;
    }

    public static boolean isBlackListDeleted(Uri uri) {
        return uri.compareTo(BLACK_LIST_URI) == 0;
    }
}
