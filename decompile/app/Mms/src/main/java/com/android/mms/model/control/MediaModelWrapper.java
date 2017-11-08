package com.android.mms.model.control;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.HashMap;

public class MediaModelWrapper {
    public static String getImageSourceBuild(Context ctx, PduPart part) {
        String str = null;
        if (!(ctx == null || part == null)) {
            Cursor cursor = SqliteWrapper.query(ctx, Uri.parse("content://mms/part_source/" + part.getDataUri().getLastPathSegment()), null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    str = cursor.getString(2);
                }
                cursor.close();
            }
        }
        return str;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static HashMap<String, String> getImageLocationSourceBuild(Context ctx, PduPart part) {
        HashMap<String, String> resultMap = new HashMap();
        if (!(ctx == null || part == null)) {
            Cursor cursor = SqliteWrapper.query(ctx, Uri.parse("content://mms/part_source/" + part.getDataUri().getLastPathSegment()), null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("islocation")) == 1) {
                            resultMap.put("islocation", "true");
                            resultMap.put("datapath", cursor.getString(cursor.getColumnIndexOrThrow("old_data")));
                            String locationTitle = cursor.getString(cursor.getColumnIndexOrThrow("locationtitle"));
                            resultMap.put("title", locationTitle);
                            String locationSub = cursor.getString(cursor.getColumnIndexOrThrow("locationsub"));
                            resultMap.put("subtitle", locationSub);
                            String latitude = cursor.getString(cursor.getColumnIndexOrThrow("latitude"));
                            resultMap.put("latitude", latitude);
                            String longitude = cursor.getString(cursor.getColumnIndexOrThrow("longitude"));
                            resultMap.put("longitude", longitude);
                            resultMap.put("locationinfo", locationTitle + "\n" + locationSub + "\n" + MessageUtils.getLocationWebLink(ctx) + latitude + "," + longitude);
                        } else {
                            resultMap.put("islocation", "false");
                            resultMap.put("datapath", cursor.getString(cursor.getColumnIndexOrThrow("old_data")));
                        }
                    }
                    cursor.close();
                } catch (IllegalArgumentException e) {
                    MLog.e("MediaModelWrapper", "IllegalArgumentException " + e.getMessage());
                } catch (Throwable th) {
                    cursor.close();
                }
            }
        }
        return resultMap;
    }
}
