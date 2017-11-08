package cn.com.xy.sms.sdk.db;

import cn.com.xy.sms.sdk.db.a.a;

/* compiled from: Unknown */
final class k extends Thread {
    k() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        Throwable th;
        XyCursor xyCursor = null;
        setName("xiaoyuan");
        XyCursor a;
        try {
            a = a.a(false, ParseItemManager.TABLE_NAME, new String[]{ParseItemManager.REGEX_TEXT}, null, null, null, null, null, "1");
            if (a != null) {
                try {
                } catch (Throwable th2) {
                    xyCursor = a;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }
}
