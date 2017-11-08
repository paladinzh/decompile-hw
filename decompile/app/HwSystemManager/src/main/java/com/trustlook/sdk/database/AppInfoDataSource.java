package com.trustlook.sdk.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;
import java.util.ArrayList;
import java.util.List;

public class AppInfoDataSource {
    private SQLiteDatabase a = null;
    private DBHelper b;
    private String[] c = new String[]{"_id", DBHelper.COLUMN_MD5, "package_name", DBHelper.COLUMN_APKPATH, DBHelper.COLUMN_SIZE, DBHelper.COLUMN_SCORE, DBHelper.COLUMN_CATEGORY, "virus_name"};

    public AppInfoDataSource(Context context) {
        if (this.b != null) {
            this.b.close();
            this.b = new DBHelper(context);
            return;
        }
        this.b = new DBHelper(context);
    }

    public synchronized void open(Context context) {
        if (this.a == null) {
            try {
                this.a = this.b.getWritableDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
                if (this.a != null) {
                    this.a.close();
                    this.b.close();
                    this.a = null;
                }
            }
        }
    }

    public void close() {
        this.b.close();
    }

    private synchronized void a(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase != null) {
            try {
                sQLiteDatabase.endTransaction();
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }
    }

    public void setAppInfoLimit(long j) {
        this.a.execSQL("DROP TRIGGER if exists delete_app_info");
        this.a.execSQL(String.format("CREATE TRIGGER if not exists delete_app_info AFTER INSERT ON table_appinfo WHEN (select count(*) from table_appinfo) > %1$s BEGIN DELETE FROM table_appinfo WHERE table_appinfo._id IN  (SELECT table_appinfo._id FROM table_appinfo ORDER BY table_appinfo._id limit (select count(*) -%1$s from table_appinfo )); END", new Object[]{String.valueOf(j)}));
    }

    public void clearAppInfoCache() {
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            this.a.beginTransaction();
            this.a.compileStatement("DELETE FROM table_appinfo").execute();
            this.a.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a(this.a);
        }
    }

    public AppInfo getAppInfoFromMD5(PkgInfo pkgInfo) {
        Exception e;
        AppInfo appInfo;
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            String[] strArr = new String[]{pkgInfo.getMd5()};
            Cursor query = this.a.query(DBHelper.TABLE_APP_INFO, this.c, "md5 = ?", strArr, null, null, null);
            AppInfo appInfo2 = null;
            while (query.moveToNext()) {
                try {
                    AppInfo appInfo3 = new AppInfo(pkgInfo.getPkgName(), pkgInfo.getMd5());
                    appInfo3.setSizeInBytes(pkgInfo.getPkgSize());
                    appInfo3.setApkPath(pkgInfo.getPkgPath());
                    String string = query.getString(query.getColumnIndex(DBHelper.COLUMN_MD5));
                    if (string == null || pkgInfo.getMd5() == null || !string.equalsIgnoreCase(pkgInfo.getMd5())) {
                        appInfo3.setScore(-1);
                        appInfo3.setCategory("");
                        appInfo3.setVirusNameInCloud("");
                    } else {
                        appInfo3.setScore(query.getInt(query.getColumnIndex(DBHelper.COLUMN_SCORE)));
                        appInfo3.setCategory(query.getString(query.getColumnIndex(DBHelper.COLUMN_CATEGORY)));
                        appInfo3.setVirusNameInCloud(query.getString(query.getColumnIndex("virus_name")));
                    }
                    appInfo2 = appInfo3;
                } catch (Exception e2) {
                    e = e2;
                    appInfo = appInfo2;
                }
            }
            if (appInfo2 != null) {
                appInfo = appInfo2;
            } else {
                appInfo = new AppInfo(pkgInfo.getPkgName(), pkgInfo.getMd5());
                appInfo.setSizeInBytes(pkgInfo.getPkgSize());
                appInfo.setApkPath(pkgInfo.getPkgPath());
                appInfo.setScore(-1);
                appInfo.setCategory("");
                appInfo.setVirusNameInCloud("");
            }
            try {
                query.close();
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                return appInfo;
            }
        } catch (Exception e4) {
            e = e4;
            appInfo = null;
            e.printStackTrace();
            return appInfo;
        }
        return appInfo;
    }

    public void batchInsertAppInfoList(List<AppInfo> list) {
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            this.a.beginTransaction();
            SQLiteStatement compileStatement = this.a.compileStatement("REPLACE INTO table_appinfo (md5, package_name, apk_size, apk_path, risk_score, risk_category, virus_name)VALUES (?, ?, ?, ?, ?, ?, ?);");
            for (AppInfo appInfo : list) {
                compileStatement.bindString(1, appInfo.getMd5() == null ? "" : appInfo.getMd5());
                compileStatement.bindString(2, appInfo.getPackageName() == null ? "" : appInfo.getPackageName());
                compileStatement.bindLong(3, appInfo.getSizeInBytes());
                compileStatement.bindString(4, appInfo.getApkPath() == null ? "" : appInfo.getApkPath());
                compileStatement.bindLong(5, (long) appInfo.getScore());
                compileStatement.bindString(6, appInfo.getCategory() == null ? "" : appInfo.getCategory());
                compileStatement.bindString(7, appInfo.getVirusNameInCloud() == null ? "" : appInfo.getVirusNameInCloud());
                compileStatement.execute();
                compileStatement.clearBindings();
            }
            this.a.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a(this.a);
        }
    }

    public long countAppInfo() {
        long j = -1;
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            this.a.beginTransaction();
            j = this.a.compileStatement("select count(*) from table_appinfo").simpleQueryForLong();
            this.a.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a(this.a);
        }
        return j;
    }

    public String getMD5FromPkgInfo(String str, String str2) {
        String str3;
        Exception e;
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            String[] strArr = new String[]{str, str2};
            Cursor query = this.a.query(DBHelper.TABLE_APP_INFO, this.c, "package_name == ? AND apk_path == ?", strArr, null, null, null);
            str3 = null;
            while (query.moveToNext()) {
                try {
                    str3 = query.getString(query.getColumnIndex(DBHelper.COLUMN_MD5));
                } catch (Exception e2) {
                    e = e2;
                }
            }
            query.close();
        } catch (Exception e3) {
            e = e3;
            str3 = null;
            e.printStackTrace();
            return str3;
        }
        return str3;
    }

    public List<AppInfo> loadAllAppInfos() {
        List<AppInfo> arrayList = new ArrayList();
        try {
            if (this.a == null) {
                this.a = this.b.getWritableDatabase();
            }
            Cursor query = this.a.query(DBHelper.TABLE_APP_INFO, this.c, null, null, null, null, null);
            query.moveToFirst();
            while (!query.isAfterLast()) {
                AppInfo appInfo = new AppInfo(query.getString(query.getColumnIndex("package_name")));
                appInfo.setMd5(query.getString(query.getColumnIndex(DBHelper.COLUMN_MD5)));
                appInfo.setScore(query.getInt(query.getColumnIndex(DBHelper.COLUMN_SCORE)));
                appInfo.setCategory(query.getString(query.getColumnIndex(DBHelper.COLUMN_CATEGORY)));
                appInfo.setVirusNameInCloud(query.getString(query.getColumnIndex("virus_name")));
                arrayList.add(appInfo);
                query.moveToNext();
            }
            query.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }
}
