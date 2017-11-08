package com.huawei.systemmanager.backup;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class BackupModuleProvider extends ContentProvider {
    private static final String QUERY_ALL_PROVIDER_METHOD = "all_module_provider_uri_query";
    private static final String TAG = "BackupModuleProvider";

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.i(TAG, "Call method is null");
            return null;
        } else if (method.equals(QUERY_ALL_PROVIDER_METHOD)) {
            return all_module_provider_uri_query(arg, extras);
        } else {
            HwLog.i(TAG, "Call method is not implemented = " + method);
            return super.call(method, arg, extras);
        }
    }

    public Bundle all_module_provider_uri_query(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        ArrayList<String> providerList = new ArrayList();
        if (!Utility.isWifiOnlyMode()) {
            providerList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider");
        }
        providerList.add("content://com.huawei.systemmanager.NotificationDBProvider");
        providerList.add("content://com.huawei.permissionmanager.provider.PermissionDataProvider");
        providerList.add("content://com.huawei.systemmanager.applockprovider");
        providerList.add("content://com.huawei.systemmanager.CommonPrefBackupProvider");
        providerList.add("content://com.huawei.android.smartpowerprovider");
        providerList.add("content://smcs");
        providerList.add("content://com.huawei.systemmanager.NetAssistantProvider");
        providerList.add("content://com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider");
        providerList.add("content://com.huawei.systemmanager.startupprovider");
        bundle.putStringArrayList("all_module_provider_uri_list", providerList);
        return bundle;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
