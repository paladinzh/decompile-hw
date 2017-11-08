package com.huawei.rcs.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Files;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsApp;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter.GroupMessageColumn;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.rcs.media.RcsMediaFileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RcsProfileUtils {
    public static void saveRcsCropImageStatus(Context context, boolean notAskMeAgain) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        MLog.i("MsgPlusProfileUtils FileTrans: ", "saveRcsCropImageStatus pref_key_crop_not_ask_me_again =  " + notAskMeAgain);
        sp.edit().putBoolean("pref_key_crop_not_ask_me_again", notAskMeAgain).commit();
    }

    public static boolean[] getRcsCropImageStatus(Context context, boolean[] preference) {
        preference[0] = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_crop_not_ask_me_again", true);
        MLog.i("MsgPlusProfileUtils FileTrans: ", "getRcsCropImageStatus pref_key_crop_not_ask_me_again =  " + preference[0]);
        return preference;
    }

    public static int getRcsMsgType(Cursor c) {
        try {
            String service_center_db = c.getString(c.getColumnIndexOrThrow("service_center"));
            MLog.v("MsgPlusProfileUtils", "service_center_db:" + service_center_db);
            if ("rcs.file".equals(service_center_db) || "rcs.image".equals(service_center_db) || "rcs.video".equals(service_center_db)) {
                return 3;
            }
            if ("rcs.location".equals(service_center_db)) {
                return 4;
            }
            if ("rcs.vcard".equals(service_center_db)) {
                return 3;
            }
            if ("rcs.groupchat.file".equals(service_center_db)) {
                return 5;
            }
            if ("rcs.mass.file".equals(service_center_db)) {
                return 6;
            }
            return 0;
        } catch (RuntimeException e) {
            MLog.e("MsgPlusProfileUtils", "RcsMsgType error");
            return -1;
        }
    }

    public static int getGroupChatRcsMsgType(Cursor c, GroupMessageColumn columnMap) {
        if (c == null || columnMap == null) {
            return -1;
        }
        return c.getInt(columnMap.columnType);
    }

    public static int getRcsMsgExtType(Cursor c) {
        try {
            String service_kind_db = c.getString(c.getColumnIndexOrThrow("service_kind"));
            if ("mcloud".equals(service_kind_db)) {
                return 2;
            }
            if ("burn".equals(service_kind_db)) {
                return 1;
            }
            if ("6".equals(service_kind_db)) {
                return 6;
            }
            if ("expression".equals(service_kind_db)) {
                return 5;
            }
            return 0;
        } catch (Exception e) {
            MLog.e("MsgPlusProfileUtils", "RcsMsgExtType error");
            return 0;
        }
    }

    public static int getRcsAnyMsgType(Cursor cursor) {
        int rcsMsgNormalType = getRcsMsgType(cursor);
        if (!(3 == rcsMsgNormalType || 5 == rcsMsgNormalType)) {
            if (6 != rcsMsgNormalType) {
                return 0;
            }
        }
        return rcsMsgNormalType;
    }

    public static void setAutoAcceptFile(Context context, boolean autoAccept) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pref_key_auto_accept_file", autoAccept);
        if (MmsApp.getDefaultTelephonyManager().isNetworkRoaming()) {
            RcsProfile.setftFileAceeptSwitch(context, 1, "pref_key_Roam_auto_accept");
            editor.putBoolean("pref_key_Roam_auto_accept", true);
            editor.putInt("autoRecieveFile", 0);
        } else {
            RcsProfile.setftFileAceeptSwitch(context, 0, "pref_key_Roam_auto_accept");
            editor.putBoolean("pref_key_Roam_auto_accept", false);
            editor.putInt("autoRecieveFile", 1);
        }
        editor.commit();
    }

    public static HashMap<String, List<String>> divideRcsFTGroups(String[] numbers) {
        HashMap<String, List<String>> map = new HashMap();
        List<String> rcs_ft = new ArrayList();
        List<String> non_rcs = new ArrayList();
        for (String number : numbers) {
            if (RcsTransaction.getFTCapabilityByNumber(number) && RcsTransaction.isFTOfflineSendAvailable(number)) {
                rcs_ft.add(number);
            } else {
                non_rcs.add(number);
            }
        }
        if (rcs_ft.size() > 0) {
            map.put("rcs_ft", rcs_ft);
        }
        if (non_rcs.size() > 0) {
            map.put("non_rcs", non_rcs);
        }
        return map;
    }

    public static boolean isImModeChangeDialogShow(Context context) {
        boolean result = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_mode_change_not_show_again", false);
        MLog.i("MsgPlusProfileUtils FileTrans: ", "isImModeChangeDialogShow pref_key_mode_change_not_show_again =  " + result);
        return result;
    }

    public static void saveImModeChangeDialogShow(Context context, boolean isShow) {
        MLog.i("MsgPlusProfileUtils FileTrans: ", "saveImModeChangeDialogShow -> pref_key_mode_change_not_show_again = " + isShow);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_mode_change_not_show_again", isShow).commit();
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, Files.getContentUri("external"), new String[]{"_id"}, "_data = ? ", new String[]{filePath}, null);
        Uri uri = null;
        if (cursor != null && cursor.moveToFirst()) {
            uri = Uri.withAppendedPath(Uri.parse("content://media/external/file"), "" + cursor.getInt(cursor.getColumnIndex("_id")));
        } else if (imageFile.exists()) {
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            values.put("media_type", Integer.valueOf(0));
            uri = context.getContentResolver().insert(Files.getContentUri("external"), values);
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return uri;
    }

    public static Uri getFileContentUri(Context context, File fileFile) {
        String filePath = fileFile.getAbsolutePath();
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, Files.getContentUri("external"), new String[]{"_id"}, "_data = ? ", new String[]{filePath}, null);
        Uri uri = null;
        if (cursor != null && cursor.moveToFirst()) {
            uri = Uri.withAppendedPath(Uri.parse("content://media/external/file"), "" + cursor.getInt(cursor.getColumnIndex("_id")));
        } else if (fileFile.exists()) {
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            uri = context.getContentResolver().insert(Files.getContentUri("external"), values);
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return uri;
    }

    public static Uri getVideoContentUri(Context context, File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, Files.getContentUri("external"), new String[]{"_id"}, "_data = ? ", new String[]{filePath}, null);
        Uri uri = null;
        if (cursor != null && cursor.moveToFirst()) {
            uri = Uri.withAppendedPath(Uri.parse("content://media/external/file"), "" + cursor.getInt(cursor.getColumnIndex("_id")));
        } else if (filePath != null) {
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            values.put("media_type", Integer.valueOf(0));
            uri = context.getContentResolver().insert(Files.getContentUri("external"), values);
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return uri;
    }

    public static void viewImageFile(Context context, String filePath, Bundle bd) {
        if (filePath == null) {
            MLog.i("MsgPlusProfileUtils FileTrans: ", "viewImageFile failed, filePath is null");
            return;
        }
        Intent viewImageInatent = new Intent("android.intent.action.VIEW");
        viewImageInatent.setDataAndType(getImageContentUri(context, new File(filePath)), RcsMediaFileUtils.getFileMimeType(filePath));
        viewImageInatent.putExtra("view-as-uri-image", true);
        viewImageInatent.putExtra("SingleItemOnly", true);
        context.startActivity(viewImageInatent);
    }

    public static boolean checkIsGroupFile(Cursor cursor) {
        try {
            int mType = cursor.getInt(cursor.getColumnIndexOrThrow(NumberInfo.TYPE_KEY));
            if (mType == 100 || mType == 101) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            MLog.e("MsgPlusProfileUtils", "RcsMsgType error");
            return false;
        }
    }
}
