package com.huawei.permissionmanager.db;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.annotation.NonNull;

class PermissionContentObserver extends ContentObserver {
    public PermissionContentObserver(Handler handler) {
        super(handler);
    }

    static void startObserve(@NonNull Context cxt, ContentObserver observer) {
        Context context = cxt.getApplicationContext();
        context.getContentResolver().registerContentObserver(DBAdapter.BLOCK_TABLE_NAME_URI, true, observer);
        context.getContentResolver().registerContentObserver(DBAdapter.REPLACE_PERMISSION_URI, true, observer);
        context.getContentResolver().registerContentObserver(DBHelper.RUNTIME_TABLE_URI, true, observer);
        context.getContentResolver().registerContentObserver(DBAdapter.COMMON_TABLE_URI, true, observer);
    }

    static void stopObserve(@NonNull Context context, ContentObserver observer) {
        if (observer != null) {
            context.getContentResolver().unregisterContentObserver(observer);
        }
    }
}
