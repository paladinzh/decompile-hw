package com.amap.api.services.core;

import android.content.Context;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

/* compiled from: ProtocalHandler */
public abstract class r<T, V> extends bt {
    protected T a;
    protected int b = 1;
    protected String c = "";
    protected Context d;
    private int h = 1;

    protected abstract String a_();

    protected abstract V b(String str) throws AMapException;

    public r(Context context, T t) {
        a(context, t);
    }

    private void a(Context context, T t) {
        this.d = context;
        this.a = t;
        this.b = 1;
        d(ServiceSettings.getInstance().getSoTimeOut());
        c(ServiceSettings.getInstance().getConnectionTimeOut());
    }

    protected V a(byte[] bArr) throws AMapException {
        String str;
        try {
            str = new String(bArr, "utf-8");
        } catch (Throwable e) {
            d.a(e, "ProtocalHandler", "loadData");
            str = null;
        }
        if (str == null || str.equals("")) {
            return null;
        }
        d.b(str);
        return b(str);
    }

    public V g() throws AMapException {
        if (this.a == null) {
            return null;
        }
        return f();
    }

    private V f() throws AMapException {
        int i = 0;
        V v = null;
        while (i < this.b) {
            try {
                byte[] a;
                int protocol = ServiceSettings.getInstance().getProtocol();
                bs a2 = bs.a(false);
                a(ac.a(this.d));
                if (protocol == 1) {
                    a = a2.a((bt) this);
                } else if (protocol != 2) {
                    a = null;
                } else {
                    a = a2.b(this);
                }
                v = b(a);
                i = this.b;
            } catch (Throwable e) {
                d.a(e, "ProtocalHandler", "getDataMayThrowAMapException");
                i++;
                if (i >= this.b) {
                    throw new AMapException(e.getErrorMessage());
                }
            } catch (Throwable e2) {
                d.a(e2, "ProtocalHandler", "getDataMayThrowAMapCoreException");
                i++;
                if (i >= this.b) {
                    h();
                    throw new AMapException(e2.a());
                }
                try {
                    Thread.sleep((long) (this.h * 1000));
                } catch (InterruptedException e3) {
                    d.a(e2, "ProtocalHandler", "getDataMayThrowInterruptedException");
                    throw new AMapException(e2.getMessage());
                }
            } catch (Throwable th) {
                d.a(th, "ProtocalHandler", "getDataMayThrowAMapCoreException");
                AMapException aMapException = new AMapException("未知的错误");
            }
        }
        return v;
    }

    public HttpEntity e() {
        try {
            String a_ = a_();
            String a = a(a_);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(a_);
            a_ = y.a();
            stringBuffer.append("&ts=" + a_);
            stringBuffer.append("&scode=" + y.a(this.d, a_, a));
            return new StringEntity(stringBuffer.toString());
        } catch (Throwable e) {
            d.a(e, "ProtocalHandler", "getEntity");
            return null;
        }
    }

    public Map<String, String> c_() {
        return null;
    }

    public Map<String, String> d_() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("User-Agent", "AMAP SDK Android Search 2.5.0");
        hashMap.put("X-INFO", y.a(this.d, l.a, null));
        hashMap.put("ia", "1");
        hashMap.put("ec", "1");
        hashMap.put("key", w.f(this.d));
        return hashMap;
    }

    private V b(byte[] bArr) throws AMapException {
        return a(bArr);
    }

    protected V h() {
        return null;
    }

    private String a(String str) {
        String[] split = str.split("&");
        Arrays.sort(split);
        StringBuffer stringBuffer = new StringBuffer();
        for (String d : split) {
            stringBuffer.append(d(d));
            stringBuffer.append("&");
        }
        String stringBuffer2 = stringBuffer.toString();
        if (stringBuffer2.length() <= 1) {
            return str;
        }
        return (String) stringBuffer2.subSequence(0, stringBuffer2.length() - 1);
    }

    protected String c(String str) {
        if (str == null) {
            return str;
        }
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Throwable e) {
            d.a(e, "ProtocalHandler", "strEncoderUnsupportedEncodingException");
            return new String();
        } catch (Throwable e2) {
            d.a(e2, "ProtocalHandler", "strEncoderException");
            return new String();
        }
    }

    protected String d(String str) {
        if (str == null) {
            return str;
        }
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (Throwable e) {
            d.a(e, "ProtocalHandler", "strReEncoder");
            return new String();
        } catch (Throwable e2) {
            d.a(e2, "ProtocalHandler", "strReEncoderException");
            return new String();
        }
    }
}
