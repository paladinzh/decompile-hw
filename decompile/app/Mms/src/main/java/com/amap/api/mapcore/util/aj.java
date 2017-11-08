package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.mapcore.s;
import com.amap.api.maps.AMapException;
import java.util.HashMap;
import java.util.Map;

/* compiled from: ProtocalHandler */
public abstract class aj<T, V> extends dj {
    protected T a;
    protected int b = 1;
    protected String c = "";
    protected Context d;
    protected final int e = 5000;
    protected final int f = 50000;
    private int j = 1;

    protected abstract V b(String str) throws AMapException;

    public aj(Context context, T t) {
        a(context, t);
    }

    private void a(Context context, T t) {
        this.d = context;
        this.a = t;
    }

    public V d() throws AMapException {
        if (this.a == null) {
            return null;
        }
        return h();
    }

    private V h() throws AMapException {
        int i = 0;
        V v = null;
        while (i < this.b) {
            try {
                di a = di.a(false);
                a(bt.a(this.d));
                v = a(a.d(this));
                i = this.b;
            } catch (Throwable e) {
                ce.a(e, "ProtocalHandler", "getDataMayThrow AMapException");
                e.printStackTrace();
                i++;
                if (i >= this.b) {
                    throw new AMapException(e.getErrorMessage());
                }
            } catch (Throwable e2) {
                ce.a(e2, "ProtocalHandler", "getDataMayThrow AMapCoreException");
                e2.printStackTrace();
                i++;
                if (i >= this.b) {
                    e();
                    throw new AMapException(e2.a());
                }
                try {
                    Thread.sleep((long) (this.j * 1000));
                } catch (InterruptedException e3) {
                    ce.a(e2, "ProtocalHandler", "getDataMayThrow InterruptedException");
                    e2.printStackTrace();
                    throw new AMapException(e2.getMessage());
                }
            }
        }
        return v;
    }

    protected V b(byte[] bArr) throws AMapException {
        String str;
        try {
            str = new String(bArr, "utf-8");
        } catch (Throwable e) {
            ce.a(e, "ProtocalHandler", "loadData Exception");
            e.printStackTrace();
            str = null;
        }
        if (str == null || str.equals("")) {
            return null;
        }
        bj.a(str);
        return b(str);
    }

    public Map<String, String> c() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("User-Agent", s.d);
        hashMap.put("X-INFO", bn.a(this.d, bj.e(), null, false));
        return hashMap;
    }

    private V a(byte[] bArr) throws AMapException {
        return b(bArr);
    }

    protected V e() {
        return null;
    }
}
