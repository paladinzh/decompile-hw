package cn.com.xy.sms.sdk.db;

import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;

/* compiled from: Unknown */
final class m implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ int b;

    m(String str, int i) {
        this.a = str;
        this.b = i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        XyCursor xyCursor;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        a.a("xy_service_data_query", 0);
        SQLiteDatabase a;
        try {
            a = cn.com.xy.sms.sdk.db.a.a.a();
            XyCursor a2;
            try {
                String str = ParseItemManager.TABLE_NAME;
                String[] strArr = new String[]{ParseItemManager.REGEX_TEXT, ParseItemManager.MATCH_ID};
                String[] strArr2 = new String[]{this.a, String.valueOf(this.b)};
                Constant.getContext();
                a2 = cn.com.xy.sms.sdk.db.a.a.a(a, false, str, strArr, "scene_id = ?  and regex_type = ?", strArr2, null, null, null, null);
                if (a2 != null) {
                    while (a2.moveToNext()) {
                        ParseItemManager.a.put(a2.getString(1), a2.getString(0));
                    }
                    if (a2 != null) {
                        XyCursor.closeCursor(a2, false);
                    }
                    cn.com.xy.sms.sdk.db.a.a.a(a);
                    return;
                }
                if (a2 != null) {
                    XyCursor.closeCursor(a2, false);
                }
                cn.com.xy.sms.sdk.db.a.a.a(a);
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = a2;
                sQLiteDatabase = a;
                th = th3;
            }
        } catch (Throwable th4) {
            th = th4;
            xyCursor = sQLiteDatabase;
            if (xyCursor != null) {
                XyCursor.closeCursor(xyCursor, false);
            }
            cn.com.xy.sms.sdk.db.a.a.a(sQLiteDatabase);
            throw th;
        }
    }
}
