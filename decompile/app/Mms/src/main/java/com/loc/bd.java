package com.loc;

import android.database.sqlite.SQLiteDatabase;

/* compiled from: DynamicFileDBCreator */
public class bd implements ai {
    private static bd a;

    private bd() {
    }

    public static synchronized bd c() {
        bd bdVar;
        synchronized (bd.class) {
            if (a == null) {
                a = new bd();
            }
            bdVar = a;
        }
        return bdVar;
    }

    public String a() {
        return "dynamicamapfile.db";
    }

    public void a(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS file (_id integer primary key autoincrement, sdkname  varchar(20), filename varchar(100),md5 varchar(20),version varchar(20),dynamicversion varchar(20),status varchar(20),reservedfield varchar(20));");
        } catch (Throwable th) {
            aa.a(th, "DynamicFileDBCreator", "onCreate");
        }
    }

    public void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
    }

    public int b() {
        return 1;
    }
}
