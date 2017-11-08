package com.huawei.hwid.core.a;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.k;
import com.huawei.hwid.core.d.l;
import java.security.MessageDigest;

public class b {
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
    private int j;
    private int k;
    private String l;
    private String m;

    public b(Context context, String str, String str2) {
        this(context, str);
        this.f = str2;
    }

    public b(Context context, String str) {
        this.j = 0;
        this.a = str;
        this.b = com.huawei.hwid.core.d.b.a();
        this.c = "";
        this.d = "";
        this.e = l.a(context, -999);
        this.f = "";
        this.g = "";
        this.h = a(context);
        this.i = "";
        this.k = 0;
        this.l = l.a();
        this.m = context.getPackageName();
        if (com.huawei.hwid.core.d.b.a(context)) {
            this.d = com.huawei.hwid.core.d.b.c(context);
        }
    }

    private String a(Context context) {
        String str = "";
        str = l.b(context);
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo.metaData == null || applicationInfo.metaData.getInt("oplog_encrypt") != 1) {
                return str;
            }
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((str + new StringBuffer().append(":WfDs").append("dbGd").append("fbhp").append(k.e("mgsI")).toString()).getBytes(XmlUtils.INPUT_ENCODING));
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                String toHexString = Integer.toHexString(b & 255);
                if (toHexString.length() == 1) {
                    stringBuffer.append("0");
                }
                stringBuffer.append(toHexString);
            }
            return stringBuffer.toString();
        } catch (Throwable e) {
            e.d("OpLogItem", e.getMessage(), e);
            return "";
        } catch (Throwable e2) {
            e.d("OpLogItem", e2.getMessage(), e2);
            return "";
        }
    }

    public void a(String str) {
        this.c = str;
    }

    public void b(String str) {
        this.g = str;
    }

    public int a() {
        return this.j;
    }

    public void c(String str) {
        this.i = str;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(this.a).append("|").append(this.b).append("|").append(this.c).append("|").append(this.d).append("|").append(this.e).append("|").append(k.f(this.f)).append("|").append(this.g).append("|").append(this.h).append("|").append(this.i).append("|").append(this.k).append("|").append(this.l).append("|").append(this.m).append("|");
        return stringBuilder.toString();
    }
}
