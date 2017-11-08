package cn.com.xy.sms.sdk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/* compiled from: Unknown */
final class d extends SQLiteOpenHelper {
    public d(Context context, String str, CursorFactory cursorFactory, int i) {
        super(context, str, null, 6);
    }

    public final void onCreate(SQLiteDatabase sQLiteDatabase) {
        try {
            c.b(sQLiteDatabase);
            c.c(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        try {
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_number_info");
        } catch (Throwable th) {
        }
        c.b(sQLiteDatabase);
    }

    public final void onOpen(SQLiteDatabase sQLiteDatabase) {
        try {
            super.onOpen(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        c.b(sQLiteDatabase);
        c.c(sQLiteDatabase);
        c.a(sQLiteDatabase, "ALTER TABLE tb_number_info ADD COLUMN t9_flag INTEGER DEFAULT '0'");
    }
}
