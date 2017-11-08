package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.XyCursor;

/* compiled from: Unknown */
public final class c {
    private static String a = "CREATE TABLE IF NOT EXISTS contacts(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone TEXT, name TEXT, data TEXT, update_time TEXT)";

    private static int a(String str, String[] strArr) {
        try {
            return cn.com.xy.sms.sdk.db.c.a("contacts", str, strArr);
        } catch (Throwable th) {
            return -1;
        }
    }

    private static long a(ContentValues contentValues) {
        long j;
        XyCursor xyCursor = null;
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        try {
            String str = "phone = ? ";
            j = new String[]{contentValues.getAsString("phone")};
            contentValues.put("update_time", String.valueOf(System.currentTimeMillis()));
            xyCursor = cn.com.xy.sms.sdk.db.c.a("contacts", new String[]{"name"}, str, (String[]) j);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    j = (long) cn.com.xy.sms.sdk.db.c.a("contacts", contentValues, str, (String[]) j);
                    XyCursor.closeCursor(xyCursor, true);
                    return j;
                }
            }
            j = cn.com.xy.sms.sdk.db.c.a("contacts", contentValues);
            return j;
        } catch (Throwable th) {
            j = new StringBuilder("ContactsManager insert: ");
            j.append(th.getMessage());
            return -1;
        } finally {
            XyCursor.closeCursor(xyCursor, true);
        }
    }

    private static XyCursor a(String[] strArr, String str, String[] strArr2, String str2) {
        try {
            return cn.com.xy.sms.sdk.db.c.a("contacts", strArr, str, strArr2, null, null, str2, null);
        } catch (Exception e) {
            return null;
        }
    }
}
