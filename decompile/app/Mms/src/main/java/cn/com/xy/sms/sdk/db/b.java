package cn.com.xy.sms.sdk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/* compiled from: Unknown */
public final class b extends SQLiteOpenHelper {
    private static final String a = "duoqu_contacts.db";
    private static final int b = 5;
    private static final String c = "CREATE TABLE IF NOT EXISTS contacts(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone TEXT, name TEXT, data TEXT, update_time TEXT)";
    private String d = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name =?";

    public b(Context context) {
        super(context, a, null, 5);
    }

    private boolean a(String str) {
        boolean z = false;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Cursor rawQuery = getReadableDatabase().rawQuery(this.d, new String[]{str});
        if (rawQuery.moveToNext() && rawQuery.getInt(0) > 0) {
            z = true;
        }
        rawQuery.close();
        return z;
    }

    public final void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL(c);
    }

    public final void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
    }
}
