package com.huawei.systemmanager.rainbow.db.assist;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RealFeatureCallMethod;
import com.huawei.systemmanager.rainbow.db.CloudDBHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class RealFeatureTableAssist {
    private static final String TAG = "RealFeatureTableAssist";

    public static int bulkInsert(CloudDBHelper dbHelper, Uri uri, ContentValues[] values) {
        return dbHelper.replaceFeatureRows(uri.getLastPathSegment(), values);
    }

    public static Cursor query(CloudDBHelper dbHelper, Uri uri, String[] projection, String selection, String[] selectionArgs) {
        HwLog.d(TAG, "query uri: " + uri);
        return dbHelper.queryGFeatureTable(uri.getLastPathSegment(), projection, selection, selectionArgs);
    }

    public static Bundle callDelete(CloudDBHelper dbHelper, String realPrefix, Bundle extras) {
        if (realPrefix == null || extras == null) {
            HwLog.e(TAG, "callDelete with error parameters!");
        } else {
            ArrayList<String> pkgNameList = extras.getStringArrayList(RealFeatureCallMethod.EXTRA_PKG_NAME_LIST_KEY);
            if (!(pkgNameList == null || pkgNameList.isEmpty())) {
                dbHelper.deleteFeatureRows(realPrefix, (List) pkgNameList);
            }
        }
        return null;
    }
}
