package com.amap.api.services.core;

import android.content.Context;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/* compiled from: BasicLBSRestHandler */
public abstract class b<T, V> extends a<T, V> {
    protected abstract String e();

    public b(Context context, T t) {
        super(context, t);
    }

    public byte[] f() {
        try {
            String e = e();
            String d = d(e);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(e);
            e = al.a();
            stringBuffer.append("&ts=" + e);
            stringBuffer.append("&scode=" + al.a(this.d, e, d));
            return stringBuffer.toString().getBytes("utf-8");
        } catch (Throwable th) {
            i.a(th, "ProtocalHandler", "getEntity");
            return null;
        }
    }

    public Map<String, String> b() {
        return null;
    }

    public Map<String, String> c() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("User-Agent", "AMAP SDK Android Search 3.2.1");
        hashMap.put("X-INFO", al.a(this.d, q.a, null, false));
        hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{"3.2.1", "sea"}));
        hashMap.put("logversion", "2.1");
        return hashMap;
    }

    protected V d() {
        return null;
    }

    private String d(String str) {
        String[] split = str.split("&");
        Arrays.sort(split);
        StringBuffer stringBuffer = new StringBuffer();
        for (String c : split) {
            stringBuffer.append(c(c));
            stringBuffer.append("&");
        }
        String stringBuffer2 = stringBuffer.toString();
        if (stringBuffer2.length() <= 1) {
            return str;
        }
        return (String) stringBuffer2.subSequence(0, stringBuffer2.length() - 1);
    }

    protected String b(String str) {
        if (str == null) {
            return str;
        }
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Throwable e) {
            i.a(e, "ProtocalHandler", "strEncoderUnsupportedEncodingException");
            return "";
        } catch (Throwable e2) {
            i.a(e2, "ProtocalHandler", "strEncoderException");
            return "";
        }
    }

    protected String c(String str) {
        if (str == null) {
            return str;
        }
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (Throwable e) {
            i.a(e, "ProtocalHandler", "strReEncoder");
            return "";
        } catch (Throwable e2) {
            i.a(e2, "ProtocalHandler", "strReEncoderException");
            return "";
        }
    }
}
