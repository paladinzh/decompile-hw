package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;

/* compiled from: Unknown */
final class i implements Runnable {
    private final /* synthetic */ ContentValues a;

    i(ContentValues contentValues) {
        this.a = contentValues;
    }

    public final void run() {
        try {
            MatchCacheManager.insertOrUpdate(this.a, 5);
        } catch (Throwable th) {
        }
    }
}
