package com.huawei.systemmanager.optimize.process;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.google.android.collect.Maps;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.Map;
import java.util.Set;

public class HwRecentsLockUtils {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.systemui.recent.HwRecentsLockProdiver");
    public static final String DATABASE_RECENTS_ID = "recent_lock_id";
    public static final String DATABASE_RECENT_LOCK_STATE = "recent_lock_state";
    public static final String DATABASE_RECENT_PKG_NAME = "recent_lock_pkgname";
    private static final String TAG = "HwRecentsLockUtils";

    public static Map<String, Boolean> search(Context context) {
        String[] projection = new String[]{DATABASE_RECENT_PKG_NAME, DATABASE_RECENT_LOCK_STATE};
        Map<String, Boolean> results = Maps.newHashMap();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, projection, null, null, null);
            if (cursor == null) {
                HwLog.w(TAG, "search, cursor is null!");
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return results;
            }
            int pkgIndex = cursor.getColumnIndex(DATABASE_RECENT_PKG_NAME);
            int lockStateIndex = cursor.getColumnIndex(DATABASE_RECENT_LOCK_STATE);
            while (cursor.moveToNext()) {
                boolean z;
                String key = cursor.getString(pkgIndex);
                if (cursor.getInt(lockStateIndex) == 1) {
                    z = true;
                } else {
                    z = false;
                }
                results.put(key, Boolean.valueOf(z));
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return results;
        } catch (Exception e22) {
            HwLog.e(TAG, "search failed!");
            e22.printStackTrace();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
            }
        }
    }

    public static Set<String> getLockedPkgs(Context ctx) {
        String[] projection = new String[]{DATABASE_RECENT_PKG_NAME, DATABASE_RECENT_LOCK_STATE};
        Set<String> results = Sets.newHashSet();
        Closeable closeable = null;
        try {
            closeable = ctx.getContentResolver().query(CONTENT_URI, projection, null, null, null);
            if (closeable == null) {
                HwLog.w(TAG, "search, cursor is null!");
                return results;
            }
            int pkgIndex = closeable.getColumnIndex(DATABASE_RECENT_PKG_NAME);
            int lockStateIndex = closeable.getColumnIndex(DATABASE_RECENT_LOCK_STATE);
            while (closeable.moveToNext()) {
                String key = closeable.getString(pkgIndex);
                if (closeable.getInt(lockStateIndex) == 1) {
                    results.add(key);
                }
            }
            Closeables.close(closeable);
            return results;
        } catch (Exception e) {
            HwLog.e(TAG, "getLockedPkgs failed!");
            e.printStackTrace();
        } finally {
            Closeables.close(closeable);
        }
    }
}
