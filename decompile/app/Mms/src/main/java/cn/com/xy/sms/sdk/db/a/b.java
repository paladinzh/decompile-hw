package cn.com.xy.sms.sdk.db.a;

import android.database.sqlite.SQLiteDatabase;

/* compiled from: Unknown */
final class b implements Runnable {
    private final /* synthetic */ String a;

    b(String str) {
        this.a = str;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            synchronized (a.a) {
                SQLiteDatabase sQLiteDatabase = null;
                try {
                    sQLiteDatabase = a.a();
                    sQLiteDatabase.execSQL(this.a);
                    a.a(sQLiteDatabase);
                } catch (Throwable th) {
                    Throwable th2 = th;
                    SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                    Throwable th3 = th2;
                    a.a(sQLiteDatabase2);
                    throw th3;
                }
            }
        } catch (Throwable th4) {
        }
    }
}
