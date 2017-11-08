package cn.com.xy.sms.sdk.db.a;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import cn.com.xy.sms.sdk.db.ParseItemManager;

/* compiled from: Unknown */
final class c extends SQLiteOpenHelper {
    public c(Context context, String str, CursorFactory cursorFactory, int i) {
        super(context, str, null, 4);
    }

    public final void onCreate(SQLiteDatabase sQLiteDatabase) {
        try {
            a.b(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        try {
            sQLiteDatabase.execSQL(ParseItemManager.DROP_TABLE);
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS tb_phone_pubid");
        } catch (Throwable th) {
        }
        a.b(sQLiteDatabase);
    }

    public final void onOpen(SQLiteDatabase sQLiteDatabase) {
        super.onOpen(sQLiteDatabase);
    }

    public final void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        a.b(sQLiteDatabase);
    }
}
