package android.rms.iaware.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant.Database;
import android.rms.iaware.AwareLog;
import java.util.List;
import java.util.Map;

public class AppTypeRecoUtils {
    private static final int APPLY_BATCH_COUNT = 100;
    private static final String TAG = "AppTypeRecoUtils";
    private static final String WHERECLAUSE = "appPkgName =? ";

    public static void insertAppTypeInfo(ContentResolver resolver, String pkgName, int version, int type, int source) {
        if (resolver != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("appPkgName", pkgName);
            cvs.put("recogVersion", Integer.valueOf(version));
            cvs.put(AppTypeRecoManager.APP_TYPE, Integer.valueOf(type));
            cvs.put("source", Integer.valueOf(source));
            try {
                resolver.insert(Database.APPTYPE_URI, cvs);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insert AppType ");
            }
        }
    }

    public static void bulkInsertAppTypeInfo(ContentResolver resolver, List<ContentValues> listContentValues) {
        if (resolver != null && listContentValues != null) {
            int i;
            int repeatCount = listContentValues.size() / 100;
            for (i = 0; i < repeatCount; i++) {
                ContentValues[] transValues = new ContentValues[100];
                for (int j = 0; j < 100; j++) {
                    transValues[j] = (ContentValues) listContentValues.get((i * 100) + j);
                }
                try {
                    resolver.bulkInsert(Database.APPTYPE_URI, transValues);
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: bulkInsert AppType ");
                }
            }
            int resetCount = listContentValues.size() % 100;
            ContentValues[] resetTransValues = new ContentValues[resetCount];
            for (i = 0; i < resetCount; i++) {
                resetTransValues[i] = (ContentValues) listContentValues.get((repeatCount * 100) + i);
            }
            try {
                resolver.bulkInsert(Database.APPTYPE_URI, resetTransValues);
            } catch (SQLiteException e2) {
                AwareLog.e(TAG, "Error: bulkInsert AppType ");
            }
        }
    }

    public static void deleteAppTypeInfo(ContentResolver resolver, String pkgName) {
        if (resolver != null && pkgName != null) {
            try {
                resolver.delete(Database.APPTYPE_URI, WHERECLAUSE, new String[]{pkgName});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteAppTypeInfo ");
            }
        }
    }

    public static void updateAppTypeInfo(ContentResolver resolver, String pkgName, int version, int type, int source) {
        if (resolver != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("recogVersion", Integer.valueOf(version));
            cvs.put(AppTypeRecoManager.APP_TYPE, Integer.valueOf(type));
            cvs.put("source", Integer.valueOf(source));
            try {
                resolver.update(Database.APPTYPE_URI, cvs, WHERECLAUSE, new String[]{pkgName});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: update AppType ");
            }
        }
    }

    public static void loadAppType(ContentResolver resolver, Map<String, Integer> typeMap) {
        if (resolver != null && typeMap != null) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(Database.APPTYPE_URI, new String[]{"appPkgName", AppTypeRecoManager.APP_TYPE}, null, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppType ");
            }
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        String pkgName = cursor.getString(0);
                        int type = cursor.getInt(1);
                        if (pkgName != null) {
                            typeMap.put(pkgName, Integer.valueOf(type));
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
    }

    public static void loadReRecogApp(ContentResolver resolver, List<String> list, int version) {
        if (resolver != null && list != null) {
            Cursor cursor = null;
            String whereClause = "source!=0 and recogVersion != ?";
            try {
                cursor = resolver.query(Database.APPTYPE_URI, new String[]{"appPkgName"}, whereClause, new String[]{String.valueOf(version)}, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadReRecogApp ");
            }
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        String pkgName = cursor.getString(0);
                        if (pkgName != null) {
                            list.add(pkgName);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
    }

    public static void deleteAppTypeInfo(ContentResolver resolver, int version) {
        if (resolver != null) {
            String whereClause = "source!=0 and recogVersion != ?";
            try {
                resolver.delete(Database.APPTYPE_URI, whereClause, new String[]{String.valueOf(version)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteAppTypeInfo ");
            }
        }
    }
}
