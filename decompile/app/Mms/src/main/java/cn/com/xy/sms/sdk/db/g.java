package cn.com.xy.sms.sdk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/* compiled from: Unknown */
final class g extends SQLiteOpenHelper {
    public g(Context context, String str, CursorFactory cursorFactory, int i) {
        super(context, str, null, 2);
    }

    public final void onCreate(SQLiteDatabase sQLiteDatabase) {
        try {
            e.b(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        try {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS tb_base_value");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS tb_conversation");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS tb_key");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS t_log");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS t_log_exception");
        } catch (Throwable th) {
            new StringBuilder("DBManager onDowngrade").append(th.getMessage());
        }
        e.b(sQLiteDatabase);
    }

    public final void onOpen(SQLiteDatabase sQLiteDatabase) {
        try {
            super.onOpen(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        e.b(sQLiteDatabase);
    }
}
