package com.huawei.hwid.core.a;

import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: OpLogInfo */
public final class b {
    private static volatile b a;
    private static final String c = q.c();
    private String b;
    private String d;
    private Queue e = new LinkedList();
    private Queue f = new LinkedList();

    public static b a(Context context) {
        if (a == null) {
            synchronized (b.class) {
                if (a == null) {
                    a = new b(context);
                }
            }
        }
        return a;
    }

    private b(Context context) {
        this.b = b(context);
        this.d = c(context);
    }

    public synchronized void a(c cVar) {
        if (this.e.offer(cVar)) {
            if (this.e.size() > 10) {
                this.e.remove();
            }
        }
    }

    public synchronized void b(c cVar) {
        if (this.f.offer(cVar)) {
            if (this.f.size() > 10) {
                this.f.remove();
            }
        }
    }

    public synchronized void a() {
        this.e.clear();
    }

    public synchronized Queue b() {
        return this.e;
    }

    public synchronized Queue c() {
        return this.f;
    }

    public synchronized void d() {
        for (c a : this.f) {
            a(a);
        }
        this.f.clear();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String toString() {
        String byteArrayOutputStream;
        OutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream2);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "OpLogReq");
            t.a(a, "clientVer", this.b);
            t.a(a, "osVersion", c);
            t.a(a, "channel", this.d);
            if (this.e != null) {
                a.startTag(null, "logList").attribute(null, "size", String.valueOf(this.e.size()));
                for (c cVar : this.e) {
                    t.a(a, "Log", cVar.toString());
                }
                a.endTag(null, "logList");
            }
            a.endTag(null, "OpLogReq");
            a.endDocument();
            byteArrayOutputStream = byteArrayOutputStream2.toString("UTF-8");
            a.b("OpLogInfo", "packedString:" + f.a(byteArrayOutputStream, true));
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e) {
                a.d("OpLogInfo", e.toString(), e);
            }
            return byteArrayOutputStream;
        } catch (Throwable e2) {
            a.d("OpLogInfo", "toString", e2);
            byteArrayOutputStream = "";
            return byteArrayOutputStream;
        } catch (Throwable e22) {
            a.d("OpLogInfo", "toString", e22);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e3) {
                a.d("OpLogInfo", e3.toString(), e3);
            }
            return byteArrayOutputStream;
        } catch (Throwable e222) {
            a.d("OpLogInfo", "toString", e222);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e32) {
                a.d("OpLogInfo", e32.toString(), e32);
            }
            return byteArrayOutputStream;
        } catch (Throwable e2222) {
            a.d("OpLogInfo", "toString", e2222);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e322) {
                a.d("OpLogInfo", e322.toString(), e322);
            }
            return byteArrayOutputStream;
        } catch (Throwable th) {
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e3222) {
                a.d("OpLogInfo", e3222.toString(), e3222);
            }
        }
    }

    private String b(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String str = "";
        if ("com.huawei.hwid".equals(context.getPackageName())) {
            String str2;
            try {
                str2 = packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (Throwable e) {
                a.d("OpLogInfo", e.toString(), e);
                str2 = str;
            }
            return "HwID " + str2;
        }
        return "SDK " + "2.1.1.202";
    }

    private String c(Context context) {
        String str = "";
        if (a.b()) {
            return "8000000";
        }
        return com.huawei.hwid.core.c.b.a(context, a.c());
    }
}
