package cn.com.xy.sms.sdk.db;

import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.db.a.a;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
final class l implements Runnable {
    l() {
    }

    public final void run() {
        SQLiteDatabase a;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            Thread.currentThread().setName("xiaoyuan-ipool" + Thread.currentThread().hashCode());
            ParseItemManager.c = System.currentTimeMillis();
            a = a.a();
            try {
                a.beginTransaction();
                Map hashMap = new HashMap(ParseItemManager.b);
                ParseItemManager.b.clear();
                Set<Entry> entrySet = hashMap.entrySet();
                String str = "UPDATE tb_regex SET last_use_time = " + System.currentTimeMillis() + " WHERE match_id" + " = ? and scene_id" + " = ?";
                for (Entry entry : entrySet) {
                    a.execSQL(str, new String[]{(String) entry.getKey(), (String) entry.getValue()});
                }
                hashMap.clear();
                if (a != null) {
                    try {
                        a.setTransactionSuccessful();
                        a.endTransaction();
                    } catch (Throwable th2) {
                    }
                }
                a.a(a);
            } catch (Throwable th3) {
                th = th3;
                if (a != null) {
                    try {
                        a.setTransactionSuccessful();
                        a.endTransaction();
                    } catch (Throwable th4) {
                    }
                }
                a.a(a);
                throw th;
            }
        } catch (Throwable th5) {
            a = null;
            th = th5;
            if (a != null) {
                a.setTransactionSuccessful();
                a.endTransaction();
            }
            a.a(a);
            throw th;
        }
    }
}
