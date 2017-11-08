package com.huawei.keyguard.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.keyguard.support.OucScreenOnCounter;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.util.HwLog;
import java.util.Set;

@TargetApi(11)
public class MagazineProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.huawei.magazineunlock");
    public static final Uri CONTENT_URI_CHANNEL = Uri.parse(CONTENT_URI + "/" + "channels");
    public static final Uri CONTENT_URI_COMMON = Uri.parse(CONTENT_URI + "/" + "common");
    public static final Uri CONTENT_URI_PICTURES = Uri.parse(CONTENT_URI + "/" + "pictures");
    public static final Uri CONTENT_URI_TEMP_UPDATED_CHANNEL = Uri.parse(CONTENT_URI + "/" + "temp_updated_channels");
    private static Uri TEMP_URI = Uri.parse("content://com.android.huawei.magazineunlock/type");
    private DatabaseHelper mDatabaseHelper;

    private static class SqlArguments {
        private final String[] args;
        private String table;
        private final String where;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = (String) url.getPathSegments().get(0);
                if (DatabaseHelper.isValidTable(this.table)) {
                    this.where = where;
                    this.args = args;
                    return;
                }
                throw new IllegalArgumentException("Bad root path: " + this.table);
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (TextUtils.isEmpty(where)) {
                this.table = (String) url.getPathSegments().get(0);
                if (DatabaseHelper.isValidTable(this.table)) {
                    this.where = where;
                    this.args = args;
                    return;
                }
                throw new IllegalArgumentException("Bad root path: " + this.table);
            } else {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                this.table = (String) url.getPathSegments().get(0);
                if (DatabaseHelper.isValidTable(this.table)) {
                    this.where = null;
                    this.args = null;
                    return;
                }
                throw new IllegalArgumentException("Bad root path: " + this.table);
            }
            throw new IllegalArgumentException("Invalid URI: " + url);
        }
    }

    public boolean onCreate() {
        this.mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        int rt;
        synchronized (MagazineProvider.class) {
            SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
            SqlArguments args = new SqlArguments(uri, where, whereArgs);
            rt = db.delete(args.table, args.where, args.args);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rt;
    }

    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        }
        return "vnd.android.cursor.item/" + args.table;
    }

    public Uri insert(Uri uri, ContentValues values) {
        synchronized (MagazineProvider.class) {
            SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
            SqlArguments args = new SqlArguments(uri);
            if (checkValidDbData(args.table, values)) {
                updatePicFormatData(values, args);
                db.insert(args.table, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return uri;
    }

    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        if (uri == null) {
            return null;
        }
        if (TEMP_URI.equals(uri)) {
            HwLog.i("MagazineProvider", "illegal uri : TEMP_URI = " + TEMP_URI);
            return null;
        }
        String table = uri.getEncodedPath();
        if (checkCaller("com.android.gallery3d") && table != null && table.contains("pictures")) {
            where = ClientHelper.getInstance().addValidConditionForWhereClause(where);
        }
        Cursor cursor = null;
        try {
            String[] strArr = projection;
            String str = where;
            String[] strArr2 = whereArgs;
            String str2 = sortOrder;
            cursor = this.mDatabaseHelper.getReadableDatabase().query(false, new SqlArguments(uri).table, strArr, str, strArr2, null, null, str2, uri.getQueryParameter("limit"));
        } catch (SQLiteException ex) {
            HwLog.w("MagazineProvider", "queryCallHwLog.ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.w("MagazineProvider", "queryCallHwLog.ex = " + ex2.toString());
        }
        return cursor;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int rt;
        synchronized (MagazineProvider.class) {
            SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
            SqlArguments args = new SqlArguments(uri, where, whereArgs);
            updatePicFormatData(values, args);
            rt = db.update(args.table, values, args.where, args.args);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rt;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        Bundle out = new Bundle(1);
        out.putInt("RESULT", -1);
        long tTime = SystemClock.uptimeMillis();
        if ("update_pref".equals(method)) {
            if (checkCaller("com.android.keyguard")) {
                writePrefData(arg, extras);
            } else if (checkCaller("com.huawei.hwstartupguide") && extras.containsKey("enable_magazinelock")) {
                boolean enable = extras.getBoolean("enable_magazinelock");
                MagazineUtils.setMagazineEnable(getContext(), enable);
                HwFyuseUtils.recordMagazineEnableStatus(enable);
            }
            out.putInt("RESULT", extras.size());
        } else if ("check_validity".equals(method)) {
            HwLog.w("MagazineProvider", "CHECK_VALIDITY from " + Binder.getCallingUid() + "; " + arg + "; " + extras);
            ClientHelper.getInstance().checkPictureValidity(getContext());
        }
        if ("get_ouc_screen_data".equals(method) && checkCaller("com.huawei.android.hwouc")) {
            HwLog.w("MagazineProvider", "UMETHOD_GET_OUC_COUNTER");
            out.putByteArray("RESULT", OucScreenOnCounter.getInst(getContext()).getUserRecords());
        }
        HwLog.w("MagazineProvider", "Call " + method + " from: " + Binder.getCallingUid() + "  use: " + (SystemClock.uptimeMillis() - tTime));
        return out;
    }

    private boolean checkCaller(String assignPkg) {
        String[] callerPackages = getContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
        if (callerPackages != null) {
            for (Object equals : callerPackages) {
                if (assignPkg.equals(equals)) {
                    return true;
                }
            }
        }
        HwLog.w("MagazineProvider", "check invalid caller: " + Binder.getCallingUid());
        return false;
    }

    private boolean checkValidDbData(String table, ContentValues values) {
        if (!"channels".equals(table)) {
            return true;
        }
        String title = (String) values.get("title");
        String version = (String) values.get("version");
        if (values.get("icon") != null || !TextUtils.isEmpty(title) || !TextUtils.isEmpty(version)) {
            return true;
        }
        HwLog.w("MagazineProvider", "this channel is invalid, ship insert");
        return false;
    }

    private void writePrefData(String file, Bundle extras) {
        Set<String> keys = null;
        if (extras != null) {
            keys = extras.keySet();
        }
        if (keys == null || keys.size() == 0) {
            HwLog.w("MagazineProvider", "Invalide ");
            return;
        }
        Editor editor = getContext().getSharedPreferences(file, 0).edit();
        for (String key : keys) {
            Object value = extras.get(key);
            if (value != null) {
                if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, ((Integer) value).intValue());
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, ((Boolean) value).booleanValue());
                } else {
                    HwLog.w("MagazineProvider", "Unsupport type for pref: " + key + " - " + value);
                }
            }
        }
        editor.apply();
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int i = 0;
        synchronized (MagazineProvider.class) {
            if (values != null) {
                if (values.length != 0) {
                    HwLog.d("MagazineProvider", "bulkInsert : uri = " + uri + ", values = " + values.length);
                    SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
                    SqlArguments args = new SqlArguments(uri);
                    int length = values.length;
                    while (i < length) {
                        ContentValues value = values[i];
                        if (checkValidDbData(args.table, value)) {
                            db.insert(args.table, null, value);
                        }
                        i++;
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    i = values.length;
                    return i;
                }
            }
            HwLog.d("MagazineProvider", "skip bulk insert : uri = " + uri);
            return 0;
        }
    }

    private void updatePicFormatData(ContentValues values, SqlArguments args) {
        if (HwFyuseUtils.isSupport3DFyuse() && args != null && "pictures".equals(args.table)) {
            String path = (String) values.get("path");
            if (path != null) {
                int fileType = HwFyuseUtils.getFileType(path);
                if (fileType != 0) {
                    values.put("picFormat", Integer.valueOf(fileType));
                }
                HwFyuseUtils.checkFileProcessStatus(path);
            }
        }
    }
}
