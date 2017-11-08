package com.amap.api.mapcore.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/* compiled from: OfflineDBCreator */
public class bl implements ft {
    private static volatile bl a;

    public static bl a() {
        if (a == null) {
            synchronized (bl.class) {
                if (a == null) {
                    a = new bl();
                }
            }
        }
        return a;
    }

    private bl() {
    }

    public void a(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item (_id integer primary key autoincrement, title  TEXT, url TEXT,mAdcode TEXT,fileName TEXT,version TEXT,lLocalLength INTEGER,lRemoteLength INTEGER,localPath TEXT,mIndex INTEGER,isProvince INTEGER NOT NULL,mCompleteCode INTEGER,mCityCode TEXT,mState INTEGER,mPinyin TEXT, UNIQUE(mAdcode));");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item_file (_id integer primary key autoincrement,mAdcode TTEXT, file TEXT);");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item_download_info (_id integer primary key autoincrement,mAdcode TEXT,fileLength integer,splitter integer,startPos integer,endPos integer, UNIQUE(mAdcode));");
        } catch (Throwable th) {
            fo.b(th, "DB", "onCreate");
            th.printStackTrace();
        }
    }

    public String b() {
        return "offlineDbV4.db";
    }

    public int c() {
        return 2;
    }

    public void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        switch (i) {
            case 1:
                sQLiteDatabase.execSQL("ALTER TABLE update_item ADD COLUMN mPinyin TEXT;");
                Cursor query = sQLiteDatabase.query("update_item", null, null, null, null, null, null);
                while (query.moveToNext()) {
                    String string = query.getString(query.getColumnIndex("url"));
                    String substring = string.substring(string.lastIndexOf("/") + 1);
                    substring = substring.substring(0, substring.lastIndexOf("."));
                    sQLiteDatabase.execSQL("update update_item set mPinyin=? where url =?", new String[]{substring, string});
                }
                query.close();
                return;
            default:
                return;
        }
    }
}
