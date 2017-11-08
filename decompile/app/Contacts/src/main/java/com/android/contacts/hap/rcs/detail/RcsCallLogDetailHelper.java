package com.android.contacts.hap.rcs.detail;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.calllog.CallLogDetailHelper;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.util.HwLog;

public class RcsCallLogDetailHelper {
    private static final Uri RCS_IM_CHAT_URI = Uri.parse("content://rcsim/chat");

    public void updateRcsInfo(Context context, Cursor cursor, PhoneCallDetails details) {
        if (context != null && cursor != null && details != null) {
            PhoneCallDetails phoneCallDetails = details;
            phoneCallDetails.setRcsInfo(cursor.getInt(CallLogDetailHelper.IS_PRIMARY_INDEX), cursor.getString(CallLogDetailHelper.SUBJECT_INDEX), cursor.getString(CallLogDetailHelper.POST_CALL_TEXT_INDEX), cursor.getString(CallLogDetailHelper.POST_CALL_VOICE_INDEX), cursor.getDouble(CallLogDetailHelper.LONGITUDE_INDEX), cursor.getDouble(CallLogDetailHelper.LATITUDE_INDEX), cursor.getLong(CallLogDetailHelper.RCS_CALL_START_TIME_INDEX), cursor.getString(CallLogDetailHelper.PICTURE_INDEX));
            getIsContainInCallData(context, details);
        }
    }

    public static void deleteRcsMapAndPicture(Context ctx, StringBuilder sb) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            RcsContactsUtils.deleteRcsMapAndPicture(ctx.getApplicationContext().getContentResolver(), sb);
        }
    }

    public static void deleteRcsMapAndPicture(Context ctx, long id) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            RcsContactsUtils.deleteRcsMapAndPicture(ctx.getApplicationContext().getContentResolver(), id);
        }
    }

    public static void getIsContainInCallData(Context context, PhoneCallDetails detail) {
        if (context != null && detail.duration != 0 && context.checkSelfPermission("android.permission.READ_SMS") == 0) {
            Cursor cursor = null;
            try {
                boolean z;
                cursor = context.getContentResolver().query(RCS_IM_CHAT_URI, new String[]{"_id"}, "PHONE_NUMBERS_EQUAL(address, ?) and date > " + String.valueOf(detail.date) + " and date - " + String.valueOf(detail.date) + " < " + String.valueOf(detail.duration * 1000), new String[]{detail.number.toString()}, null);
                if (cursor == null || cursor.getCount() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                detail.mIsContainInCallData = z;
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e) {
                HwLog.e("RcsCallLogDetailHelper", "Error appears when query in-call data from mms thread.");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
