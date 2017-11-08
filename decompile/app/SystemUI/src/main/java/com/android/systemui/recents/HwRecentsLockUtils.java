package com.android.systemui.recents;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.Log;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.recents.model.Task;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import java.util.HashMap;
import java.util.Map;

public class HwRecentsLockUtils {
    private static Map<String, Boolean> lockStateMap = null;

    private static void insert(Context context, int id, boolean locked, String pkgname) {
        Log.d("HwRecentsLockUtils", "insert: " + pkgname + " " + locked);
        ContentResolver resolver = context.getContentResolver();
        ContentValues value = new ContentValues();
        value.put("recent_lock_id", Integer.valueOf(id));
        value.put("recent_lock_state", Boolean.valueOf(locked));
        value.put("recent_lock_pkgname", pkgname);
        resolver.insert(HwRecentsLockProdiver.AUTHORITY_URI, value);
    }

    public static synchronized Map<String, Boolean> refreshToCache() {
        Map<String, Boolean> map;
        synchronized (HwRecentsLockUtils.class) {
            lockStateMap = search(HwSystemUIApplication.getContext());
            map = lockStateMap;
        }
        return map;
    }

    public static synchronized Map<String, Boolean> searchFromCache() {
        synchronized (HwRecentsLockUtils.class) {
            if (lockStateMap == null) {
                Log.e("HwRecentsLockUtils", "when call searchFromCache, lockStateMap is null!!");
                Map hashMap = new HashMap();
                return hashMap;
            }
            Map<String, Boolean> map = lockStateMap;
            return map;
        }
    }

    public static Map<String, Boolean> search(Context context) {
        Log.d("HwRecentsLockUtils", "search");
        Context context2 = context;
        CursorLoader cursorLoader = new CursorLoader(context2, HwRecentsLockProdiver.AUTHORITY_URI, new String[]{"recent_lock_pkgname", "recent_lock_state"}, null, null, null);
        Cursor cursor = null;
        Map<String, Boolean> results = new HashMap();
        try {
            cursor = cursorLoader.loadInBackground();
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return results;
            }
            if (cursor.moveToFirst()) {
                do {
                    boolean z;
                    String key = cursor.getString(cursor.getColumnIndex("recent_lock_pkgname"));
                    if (cursor.getInt(cursor.getColumnIndex("recent_lock_state")) == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    results.put(key, Boolean.valueOf(z));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Exception e) {
            Log.d("HwRecentsLockUtils", e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int update(Context context, String pkgname, boolean state) {
        Log.d("HwRecentsLockUtils", "update: " + pkgname + " " + state);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("recent_lock_state", Boolean.valueOf(state));
        return resolver.update(HwRecentsLockProdiver.AUTHORITY_URI, values, "recent_lock_pkgname=?", new String[]{pkgname});
    }

    public static boolean isLocked(String pkgName, boolean def) {
        Map<String, Boolean> map = searchFromCache();
        Boolean locked = Boolean.valueOf(def);
        if (map.get(pkgName) != null) {
            locked = (Boolean) map.get(pkgName);
        }
        return locked.booleanValue();
    }

    public static void insertOrUpdate(final Context mContext, final Task mTask) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                Map<String, Boolean> map = HwRecentsLockUtils.search(mContext);
                if (mTask != null) {
                    String pkgname = mTask.packageName;
                    if (map.get(pkgname) != null) {
                        HwRecentsLockUtils.update(mContext, pkgname, mTask.isLocked);
                    } else {
                        HwRecentsLockUtils.insert(mContext, mTask.key.id, mTask.isLocked, pkgname);
                    }
                    Log.i("HwRecentsLockUtils", (mTask.isLocked ? " lock: " : "unlock: ") + mTask.packageName);
                    HwRecentsLockUtils.refreshToCache();
                }
                return true;
            }
        });
    }
}
