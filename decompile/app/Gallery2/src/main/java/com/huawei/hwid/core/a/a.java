package com.huawei.hwid.core.a;

import android.content.Context;
import android.content.pm.PackageManager;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.d;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.core.d.p;
import com.huawei.hwid.core.encrypt.f;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import org.xmlpull.v1.XmlSerializer;
import tmsdk.common.TMSDKContext;

public final class a {
    private static volatile a a;
    private static final String c = l.c();
    private String b;
    private String d;
    private Queue<b> e = new LinkedList();
    private Queue<b> f = new LinkedList();

    public static a a(Context context) {
        if (a == null) {
            synchronized (a.class) {
                if (a == null) {
                    a = new a(context);
                }
            }
        }
        return a;
    }

    private a(Context context) {
        this.b = b(context);
        this.d = c(context);
    }

    public synchronized void a(b bVar) {
        if (this.e.offer(bVar)) {
            if (this.e.size() > 10) {
                this.e.remove();
            }
        }
    }

    public synchronized void b(b bVar) {
        if (this.f.offer(bVar)) {
            if (this.f.size() > 10) {
                this.f.remove();
            }
        }
    }

    public synchronized void a() {
        this.e.clear();
    }

    public synchronized Queue<b> b() {
        return this.e;
    }

    public synchronized Queue<b> c() {
        return this.f;
    }

    public synchronized void d() {
        for (b a : this.f) {
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
            XmlSerializer a = p.a(byteArrayOutputStream2);
            a.startDocument(XmlUtils.INPUT_ENCODING, Boolean.valueOf(true));
            a.startTag(null, "OpLogReq");
            p.a(a, "clientVer", this.b);
            p.a(a, "osVersion", c);
            p.a(a, TMSDKContext.CON_CHANNEL, this.d);
            if (this.e != null) {
                a.startTag(null, "logList").attribute(null, "size", String.valueOf(this.e.size()));
                for (b bVar : this.e) {
                    p.a(a, "Log", bVar.toString());
                }
                a.endTag(null, "logList");
            }
            a.endTag(null, "OpLogReq");
            a.endDocument();
            byteArrayOutputStream = byteArrayOutputStream2.toString(XmlUtils.INPUT_ENCODING);
            e.b("OpLogInfo", "packedString:" + f.a(byteArrayOutputStream, true));
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e) {
                e.d("OpLogInfo", e.getMessage(), e);
            }
            return byteArrayOutputStream;
        } catch (Throwable e2) {
            e.d("OpLogInfo", "toString", e2);
            byteArrayOutputStream = "";
            return byteArrayOutputStream;
        } catch (Throwable e22) {
            e.d("OpLogInfo", "toString", e22);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e3) {
                e.d("OpLogInfo", e3.getMessage(), e3);
            }
            return byteArrayOutputStream;
        } catch (Throwable e222) {
            e.d("OpLogInfo", "toString", e222);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e32) {
                e.d("OpLogInfo", e32.getMessage(), e32);
            }
            return byteArrayOutputStream;
        } catch (Throwable e2222) {
            e.d("OpLogInfo", "toString", e2222);
            byteArrayOutputStream = "";
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e322) {
                e.d("OpLogInfo", e322.getMessage(), e322);
            }
            return byteArrayOutputStream;
        } catch (Throwable th) {
            try {
                byteArrayOutputStream2.close();
            } catch (Throwable e3222) {
                e.d("OpLogInfo", e3222.getMessage(), e3222);
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
                e.d("OpLogInfo", e.getMessage(), e);
                str2 = str;
            }
            return "HwID " + str2;
        }
        return "SDK " + "2.4.0.300";
    }

    private String c(Context context) {
        String str = "";
        if (d.b()) {
            return "8000000";
        }
        return com.huawei.hwid.core.d.a.a(context, d.c());
    }
}
