package com.huawei.systemmanager.rainbow.client.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.AddViewValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BootstartupValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudCommonValue;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.GetapplistValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationExValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import java.util.ArrayList;

public class UpdateOuterTableUtil {
    public static void updateOuterTable(Context context) {
        updatePermissionTableGenByView(context);
        updateCommonTableGenByView(context);
        updateNetworkTableGenByView(context);
    }

    private static void updatePermissionTableGenByView(Context context) {
        Cursor cursor = context.getContentResolver().query(PermissionValues.PERMISSION_OUTERVIEW_URI, null, null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            ArrayList<ContentValues> contentValuesList = new ArrayList();
            ContentValues contentValues = new ContentValues();
            int pkgNameIndex = cursor.getColumnIndex("packageName");
            int permissionCodeIndex = cursor.getColumnIndex("permissionCode");
            int permissionCfgIndex = cursor.getColumnIndex("permissionCfg");
            int permissionTrustIndex = cursor.getColumnIndex("trust");
            while (cursor.moveToNext()) {
                contentValues.put("packageName", cursor.getString(pkgNameIndex));
                contentValues.put("permissionCode", Integer.valueOf(cursor.getInt(permissionCodeIndex)));
                contentValues.put("permissionCfg", Integer.valueOf(cursor.getInt(permissionCfgIndex)));
                contentValues.put("trust", cursor.getString(permissionTrustIndex));
                contentValuesList.add(new ContentValues(contentValues));
                contentValues.clear();
            }
            cursor.close();
            if (!contentValuesList.isEmpty()) {
                ProviderUtils.deleteAll(context, PermissionValues.PERMISSION_OUTERTABLE_URI);
                context.getContentResolver().bulkInsert(PermissionValues.PERMISSION_OUTERTABLE_URI, (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
            }
        }
    }

    private static void updateCommonTable(Context context, Uri srcUri, Uri desUri, String srcCfgName, String desCfgName, String desPkgName) {
        Cursor cursor = context.getContentResolver().query(srcUri, null, null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            ArrayList<ContentValues> contentValuesList = new ArrayList();
            ContentValues contentValues = new ContentValues();
            int pkgNameIndex = cursor.getColumnIndex("packageName");
            int permissionCfgIndex = cursor.getColumnIndex(srcCfgName);
            while (cursor.moveToNext()) {
                contentValues.put(desPkgName, cursor.getString(pkgNameIndex));
                contentValues.put(desCfgName, cursor.getString(permissionCfgIndex));
                contentValuesList.add(new ContentValues(contentValues));
                contentValues.clear();
            }
            cursor.close();
            if (!contentValuesList.isEmpty()) {
                ProviderUtils.deleteAll(context, desUri);
                context.getContentResolver().bulkInsert(desUri, (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
            }
        }
    }

    private static void updateCommonTableGenByView(Context context) {
        updateCommonTable(context, CloudCommonValue.BOOTSTARTUP_OUTERVIEW_URI, BootstartupValues.CONTENT_OUTERTABLE_URI, "bootstartupDefaultValue", "permissionCfg", "packageName");
        updateCommonTable(context, CloudCommonValue.ADDVIEW_OUTERVIEW_URI, AddViewValues.CONTENT_OUTERTABLE_URI, "addviewDefaultValue", "permissionCfg", "packageName");
        updateCommonTable(context, CloudCommonValue.SEND_NOTIFICATION_OUTERVIEW_URI, NotificationValues.CONTENT_OUTERTABLE_URI, "sendNotificationDefaultValue", "permissionCfg", "packageName");
        updateCommonTable(context, CloudCommonValue.NOTIFICATION_SIGNAL_OUTERVIEW_URI, NotificationExValues.CONTENT_OUTERTABLE_URI, "notificationSignalDefaultValue", "permissionCfg", "packageName");
        updateCommonTable(context, CloudCommonValue.GET_APPLIST_OUTERVIEW_URI, GetapplistValues.CONTENT_OUTERTABLE_URI, "getapplistDefaultValue", "permissionCfg", "packageName");
    }

    private static void updateNetworkTableGenByView(Context context) {
        Cursor cursor = context.getContentResolver().query(NetworkValues.OUTER_VIEW_URI, null, null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            ArrayList<ContentValues> contentValuesList = new ArrayList();
            ContentValues contentValues = new ContentValues();
            int pkgNameIndex = cursor.getColumnIndex("packageName");
            int networkDataIndex = cursor.getColumnIndex("netDataPermission");
            int networkWifiIndex = cursor.getColumnIndex("netWifiPermission");
            while (cursor.moveToNext()) {
                contentValues.put("packageName", cursor.getString(pkgNameIndex));
                contentValues.put("netDataPermission", cursor.getString(networkDataIndex));
                contentValues.put("netWifiPermission", cursor.getString(networkWifiIndex));
                contentValuesList.add(new ContentValues(contentValues));
                contentValues.clear();
            }
            cursor.close();
            if (!contentValuesList.isEmpty()) {
                ProviderUtils.deleteAll(context, NetworkValues.OUTER_TABLE_URI);
                context.getContentResolver().bulkInsert(NetworkValues.OUTER_TABLE_URI, (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
            }
        }
    }
}
