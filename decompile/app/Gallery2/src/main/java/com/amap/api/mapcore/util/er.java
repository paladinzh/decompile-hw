package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.Map;

/* compiled from: BasicHandler */
public abstract class er<T, V> extends hd {
    protected T a;
    protected int b = 1;
    protected String c = "";
    protected Context d;
    protected String e;
    private int i = 1;

    protected abstract V a(String str) throws eq;

    public er(Context context, T t) {
        a(context, t);
    }

    private void a(Context context, T t) {
        this.d = context;
        this.a = t;
        this.b = 1;
        b(30000);
        a(30000);
    }

    protected V a(byte[] bArr) throws eq {
        String str;
        try {
            str = new String(bArr, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        if (str == null || str.equals("")) {
            return null;
        }
        et.a(str, this.e);
        return a(str);
    }

    public V d() throws eq {
        if (this.a == null) {
            return null;
        }
        return f();
    }

    private V f() throws eq {
        int i = 0;
        V v = null;
        while (i < this.b) {
            try {
                hc a = hc.a(false);
                a(ff.a(this.d));
                v = b(a(1, a, this));
                i = this.b;
            } catch (ex e) {
                i++;
                if (i >= this.b) {
                    e();
                    if ("http连接失败 - ConnectionException".equals(e.getMessage()) || "socket 连接异常 - SocketException".equals(e.getMessage()) || "未知的错误".equals(e.a()) || "服务器连接失败 - UnknownServiceException".equals(e.getMessage())) {
                        throw new eq("http或socket连接失败 - ConnectionException");
                    }
                    throw new eq(e.a());
                }
                try {
                    Thread.sleep((long) (this.i * 1000));
                } catch (InterruptedException e2) {
                    if ("http连接失败 - ConnectionException".equals(e.getMessage()) || "socket 连接异常 - SocketException".equals(e.getMessage()) || "服务器连接失败 - UnknownServiceException".equals(e.getMessage())) {
                        throw new eq("http或socket连接失败 - ConnectionException");
                    }
                    throw new eq(e.a());
                }
            } catch (eq e3) {
                i++;
                if (i >= this.b) {
                    throw new eq(e3.a());
                }
            } catch (Throwable th) {
                eq eqVar = new eq("未知错误");
            }
        }
        return v;
    }

    protected byte[] a(int i, hc hcVar, hd hdVar) throws ex {
        if (i == 1) {
            return hcVar.b(hdVar);
        }
        if (i != 2) {
            return null;
        }
        return hcVar.a(hdVar);
    }

    public Map<String, String> b() {
        return null;
    }

    public Map<String, String> a() {
        return null;
    }

    private V b(byte[] bArr) throws eq {
        return a(bArr);
    }

    protected V e() {
        return null;
    }
}
