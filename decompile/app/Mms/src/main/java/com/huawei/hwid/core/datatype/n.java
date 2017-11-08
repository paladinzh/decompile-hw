package com.huawei.hwid.core.datatype;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.j;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.encrypt.f;
import java.security.SecureRandom;

/* compiled from: VipDeviceInfo */
public class n {
    static int d = -1;
    String a = "";
    String b = "";
    String c = "";
    private Context e;

    public n(Context context) {
        this.e = context;
    }

    public boolean a() {
        if (!d.h(this.e)) {
            return false;
        }
        this.b = q.d();
        if (TextUtils.isEmpty(this.b)) {
            return false;
        }
        this.a = i();
        this.c = f();
        return true;
    }

    public static int b() {
        return 1;
    }

    public String c() {
        return this.a;
    }

    public String d() {
        return this.c;
    }

    public String e() {
        return this.b;
    }

    public String f() {
        String str = "";
        try {
            byte[] a = q.a("TDID", this.a);
            if (a != null && a.length > 0) {
                str = Base64.encodeToString(a, 10);
            }
        } catch (Exception e) {
            a.d("VipDeviceInfo", "call getSign cause:" + e.toString());
        }
        return str;
    }

    private static String i() {
        StringBuffer stringBuffer = new StringBuffer();
        String a = d.a();
        int nextInt = (new SecureRandom().nextInt(10000) + 10000) % 10000;
        if (nextInt < 1000) {
            nextInt += 1000;
        }
        stringBuffer.append(a).append(nextInt);
        String stringBuffer2 = stringBuffer.toString();
        a.b("VipDeviceInfo", "salt:" + f.a(stringBuffer2));
        return stringBuffer2;
    }

    public static boolean g() {
        return h() > 0;
    }

    public static synchronized int h() {
        synchronized (n.class) {
            if (-1 == d) {
                try {
                    Object a = j.a("android.os.SystemProperties", "getInt", new Class[]{String.class, Integer.TYPE}, new Object[]{"ro.product.member.level", Integer.valueOf(0)});
                    if (a != null) {
                        d = ((Integer) a).intValue();
                    }
                } catch (Exception e) {
                    a.d("VipDeviceInfo", e.toString());
                    d = 0;
                }
                a.b("VipDeviceInfo", "current terminal vipLevel:" + d);
                int i = d;
                return i;
            }
            i = d;
            return i;
        }
    }
}
