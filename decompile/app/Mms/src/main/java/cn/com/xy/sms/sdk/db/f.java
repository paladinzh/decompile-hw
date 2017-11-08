package cn.com.xy.sms.sdk.db;

import android.database.sqlite.SQLiteDatabase;

/* compiled from: Unknown */
final class f implements Runnable {
    private final /* synthetic */ String a;

    f(String str) {
        this.a = str;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            synchronized (e.a) {
                SQLiteDatabase sQLiteDatabase = null;
                try {
                    sQLiteDatabase = e.a();
                    sQLiteDatabase.execSQL(this.a);
                    e.a(sQLiteDatabase);
                } catch (Throwable th) {
                    Throwable th2 = th;
                    SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                    Throwable th3 = th2;
                    e.a(sQLiteDatabase2);
                    throw th3;
                }
            }
        } catch (Throwable th4) {
        }
    }
}
