package com.huawei.hwid.core.a;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.encrypt.f;
import java.security.MessageDigest;

/* compiled from: OpLogItem */
public class c {
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
    private String k;
    private String l;

    public c(Context context, String str, String str2) {
        this(context, str);
        this.f = str2;
    }

    public c(Context context, String str) {
        this.a = str;
        this.b = d.a();
        this.c = "";
        this.d = "";
        this.e = q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL);
        this.f = "";
        this.g = "";
        this.h = a(context);
        this.i = "";
        this.j = 0;
        this.k = q.a();
        this.l = context.getPackageName();
        if (d.a(context)) {
            this.d = d.c(context);
        }
    }

    private String a(Context context) {
        String str = "";
        str = q.b(context);
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo.metaData == null || applicationInfo.metaData.getInt("oplog_encrypt") != 1) {
                return str;
            }
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((str + new StringBuffer().append(":WfDs").append("dbGd").append("fbhp").append(p.g("mgsI")).toString()).getBytes("UTF-8"));
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
            a.d("OpLogItem", e.getMessage(), e);
            return "";
        } catch (Throwable e2) {
            a.d("OpLogItem", e2.getMessage(), e2);
            return "";
        }
    }

    public void a(String str) {
        this.c = str;
    }

    public void b(String str) {
        this.f = str;
    }

    public void c(String str) {
        this.g = str;
    }

    public void a(boolean z) {
        if (z) {
            this.j = 1;
        } else {
            this.j = 0;
        }
    }

    public void d(String str) {
        this.i = str;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(this.a).append("|").append(this.b).append("|").append(this.c).append("|").append(this.d).append("|").append(this.e).append("|").append(f.c(this.f)).append("|").append(this.g).append("|").append(this.h).append("|").append(this.i).append("|").append(this.j).append("|").append(this.k).append("|").append(this.l).append("|");
        return stringBuilder.toString();
    }
}
