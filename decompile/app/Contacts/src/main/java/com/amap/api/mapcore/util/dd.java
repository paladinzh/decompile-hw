package com.amap.api.mapcore.util;

import com.amap.api.maps.AMapException;
import java.net.Proxy;

/* compiled from: BaseNetManager */
public class dd {
    private static dd a;

    public static dd a() {
        if (a == null) {
            a = new dd();
        }
        return a;
    }

    public byte[] a(dj djVar) throws bk {
        bk e;
        try {
            dl a = a(djVar, true);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] b(dj djVar) throws bk {
        bk e;
        try {
            dl a = a(djVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            cb.a(th, "BaseNetManager", "makeSyncPostRequest");
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    protected void c(dj djVar) throws bk {
        if (djVar == null) {
            throw new bk("requeust is null");
        } else if (djVar.a() == null || "".equals(djVar.a())) {
            throw new bk("request url is empty");
        }
    }

    protected dl a(dj djVar, boolean z) throws bk {
        bk e;
        Proxy proxy = null;
        try {
            c(djVar);
            if (djVar.i != null) {
                proxy = djVar.i;
            }
            return new df(djVar.g, djVar.h, proxy, z).a(djVar.f(), djVar.c(), djVar.g());
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }
}
