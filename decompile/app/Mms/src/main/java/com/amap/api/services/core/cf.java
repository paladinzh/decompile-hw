package com.amap.api.services.core;

import com.amap.api.maps.AMapException;
import java.net.Proxy;

/* compiled from: BaseNetManager */
public class cf {
    public byte[] a(cj cjVar) throws ai {
        ai e;
        try {
            cl a = a(cjVar, true);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] b(cj cjVar) throws ai {
        ai e;
        try {
            cl a = a(cjVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            ay.a(th, "BaseNetManager", "makeSyncPostRequest");
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    protected void c(cj cjVar) throws ai {
        if (cjVar == null) {
            throw new ai("requeust is null");
        } else if (cjVar.g() == null || "".equals(cjVar.g())) {
            throw new ai("request url is empty");
        }
    }

    protected cl a(cj cjVar, boolean z) throws ai {
        ai e;
        Proxy proxy = null;
        try {
            c(cjVar);
            if (cjVar.g != null) {
                proxy = cjVar.g;
            }
            return new cg(cjVar.e, cjVar.f, proxy, z).a(cjVar.n(), cjVar.c(), cjVar.o());
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }
}
