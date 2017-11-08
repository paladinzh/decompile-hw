package com.huawei.systemmanager.util.numberlocation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;

public class NumberLocationHelper {
    private static final int COLINDEX_GEOLOCATION = 1;
    private static final int LOCATION_ATTRIBUTES_LEN = 2;
    private static final int LOCATION_INDEX = 0;
    private static final int LOCATION_LEN = 1;
    private static final Uri NUMBER_LOCATION_URI = Uri.parse("content://com.huawei.numberlocation/numberlocation");
    private static final int OPERATOR_INDEX = 1;
    private static final String[] PROJECTIONS = new String[]{"number", "geolocation"};
    private static final String TAG = NumberLocationHelper.class.getName();

    public static NumberLocationInfo queryNumberLocation(Context context, String number) {
        NumberLocationInfo location = new NumberLocationInfo();
        if (!CustomizeWrapper.isNumberLocationEnabled()) {
            return location;
        }
        if (context == null || TextUtils.isEmpty(number)) {
            HwLog.w(TAG, "queryNumberLocation: Invalid params");
            return location;
        }
        try {
            Cursor c = context.getContentResolver().query(NUMBER_LOCATION_URI.buildUpon().appendQueryParameter("showLocation", "true").build(), PROJECTIONS, "number = ?", new String[]{number}, null);
            if (Utility.isNullOrEmptyCursor(c, true)) {
                HwLog.w(TAG, "queryNumberLocation : Failed");
                return location;
            }
            if (c.moveToFirst()) {
                location = parseNumberLocation(c.getString(1));
            } else {
                HwLog.w(TAG, "queryNumberLocation : Invalid query result");
            }
            c.close();
            return location;
        } catch (Exception e) {
            HwLog.e(TAG, "queryNumberLocation: Exception", e);
        }
    }

    public static NumberLocationInfo parseNumberLocation(String geolocation) {
        NumberLocationInfo location = new NumberLocationInfo();
        if (TextUtils.isEmpty(geolocation)) {
            return location;
        }
        String[] infos = geolocation.split(" +");
        if (2 == infos.length) {
            location.setLocation(infos[0]);
            location.setOperator(infos[1]);
        } else if (1 == infos.length) {
            location.setLocation(infos[0]);
        } else {
            HwLog.w(TAG, "parseNumberLocation: Invalid location info");
            return location;
        }
        return location;
    }
}
