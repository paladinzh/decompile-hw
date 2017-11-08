package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.maps.AMapException;
import java.util.Map;

/* compiled from: BasicHandler */
public abstract class a<T, V> extends cj {
    protected T a;
    protected int b = 1;
    protected String c = "";
    protected Context d;
    private int h = 1;

    protected abstract V a(String str) throws AMapException;

    public a(Context context, T t) {
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
            i.a(e, "ProtocalHandler", "loadData");
            str = null;
        }
        if (str == null || str.equals("")) {
            return null;
        }
        i.b(str);
        return a(str);
    }

    public V a() throws AMapException {
        if (this.a == null) {
            return null;
        }
        return e();
    }

    private V e() throws AMapException {
        int i = 0;
        V v = null;
        while (i < this.b) {
            try {
                int protocol = ServiceSettings.getInstance().getProtocol();
                ci a = ci.a(false);
                a(aq.a(this.d));
                v = b(a(protocol, a, this));
                i = this.b;
            } catch (Throwable e) {
                i.a(e, "ProtocalHandler", "getDataMayThrowAMapCoreException");
                i++;
                if (i >= this.b) {
                    d();
                    if (AMapException.ERROR_CONNECTION.equals(e.getMessage()) || AMapException.ERROR_SOCKET.equals(e.getMessage()) || AMapException.ERROR_UNKNOWN.equals(e.a()) || AMapException.ERROR_UNKNOW_SERVICE.equals(e.getMessage())) {
                        throw new AMapException(AMapException.AMAP_CLIENT_NETWORK_EXCEPTION);
                    }
                    throw new AMapException(e.a());
                }
                try {
                    Thread.sleep((long) (this.h * 1000));
                } catch (InterruptedException e2) {
                    i.a(e, "ProtocalHandler", "getDataMayThrowInterruptedException");
                    if (AMapException.ERROR_CONNECTION.equals(e.getMessage()) || AMapException.ERROR_SOCKET.equals(e.getMessage()) || AMapException.ERROR_UNKNOW_SERVICE.equals(e.getMessage())) {
                        throw new AMapException(AMapException.AMAP_CLIENT_NETWORK_EXCEPTION);
                    }
                    throw new AMapException(e.a());
                }
            } catch (Throwable e3) {
                i.a(e3, "ProtocalHandler", "getDataMayThrowAMapException");
                i++;
                if (i >= this.b) {
                    throw new AMapException(e3.getErrorMessage());
                }
            } catch (Throwable th) {
                i.a(th, "ProtocalHandler", "getDataMayThrowAMapCoreException");
                AMapException aMapException = new AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR);
            }
        }
        return v;
    }

    protected byte[] a(int i, ci ciVar, cj cjVar) throws ai {
        if (i == 1) {
            return ciVar.b(cjVar);
        }
        if (i != 2) {
            return null;
        }
        return ciVar.a(cjVar);
    }

    public Map<String, String> b() {
        return null;
    }

    public Map<String, String> c() {
        return null;
    }

    private V b(byte[] bArr) throws AMapException {
        return a(bArr);
    }

    protected V d() {
        return null;
    }
}
