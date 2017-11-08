package com.amap.api.mapcore.util;

import android.database.sqlite.SQLiteDatabase;

/* compiled from: OfflineDBCreator */
public class w implements cj {
    private static volatile w a;

    public static w a() {
        if (a == null) {
            synchronized (w.class) {
                if (a == null) {
                    a = new w();
                }
            }
        }
        return a;
    }

    private w() {
    }

    public void a(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item (_id integer primary key autoincrement, title  TEXT, url TEXT,mAdcode TEXT,fileName TEXT,version TEXT,lLocalLength INTEGER,lRemoteLength INTEGER,localPath TEXT,mIndex INTEGER,isProvince INTEGER NOT NULL,mCompleteCode INTEGER,mCityCode TEXT,mState INTEGER, UNIQUE(mAdcode));");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item_file (_id integer primary key autoincrement,mAdcode TTEXT, file TEXT);");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS update_item_download_info (_id integer primary key autoincrement,mAdcode TEXT,fileLength integer,splitter integer,startPos integer,endPos integer, UNIQUE(mAdcode));");
        } catch (Throwable th) {
            ce.a(th, "DB", "onCreate");
            th.printStackTrace();
        }
    }

    public String b() {
        return "offlineDbV4.db";
    }

    public int c() {
        return 1;
    }

    public void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
    }
}
