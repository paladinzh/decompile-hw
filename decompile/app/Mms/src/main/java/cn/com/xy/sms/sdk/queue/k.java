package cn.com.xy.sms.sdk.queue;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.c;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class k {
    private static String d = "CREATE TABLE IF NOT EXISTS contacts(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone TEXT, name TEXT, data TEXT, update_time TEXT)";
    int a;
    HashMap<String, String> b;
    private Map c;

    public k(int i, String... strArr) {
        int i2 = 0;
        this.b = null;
        this.c = null;
        this.a = i;
        if (strArr.length % 2 == 0 && strArr.length > 0) {
            this.b = new HashMap();
            int length = strArr.length;
            while (i2 < length) {
                if (!(strArr[i2 + 1] == null || strArr[i2 + 1].equals(""))) {
                    this.b.put(strArr[i2], strArr[i2 + 1]);
                }
                i2 += 2;
            }
        }
    }

    private static int a(String str, String[] strArr) {
        try {
            return c.a("contacts", str, strArr);
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
            xyCursor = c.a("contacts", new String[]{"name"}, str, (String[]) j);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    j = (long) c.a("contacts", contentValues, str, (String[]) j);
                    XyCursor.closeCursor(xyCursor, true);
                    return j;
                }
            }
            j = c.a("contacts", contentValues);
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
            return c.a("contacts", strArr, str, strArr2, null, null, str2, null);
        } catch (Exception e) {
            return null;
        }
    }

    public final Map a() {
        return this.c;
    }

    public final void a(Map map) {
        this.c = map;
    }
}
