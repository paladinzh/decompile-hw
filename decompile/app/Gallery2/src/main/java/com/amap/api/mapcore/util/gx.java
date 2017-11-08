package com.amap.api.mapcore.util;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/* compiled from: BaseNetManager */
public class gx {
    private static gx a;

    /* compiled from: BaseNetManager */
    public interface a {
        URLConnection a(Proxy proxy, URL url);
    }

    public static gx a() {
        if (a == null) {
            a = new gx();
        }
        return a;
    }

    public byte[] a(hd hdVar) throws ex {
        ex e;
        try {
            hf a = a(hdVar, true);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new ex("未知的错误");
        }
    }

    public byte[] b(hd hdVar) throws ex {
        ex e;
        try {
            hf a = a(hdVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            fl.a(th, "BaseNetManager", "makeSyncPostRequest");
            e2 = new ex("未知的错误");
        }
    }

    protected void c(hd hdVar) throws ex {
        if (hdVar == null) {
            throw new ex("requeust is null");
        } else if (hdVar.c() == null || "".equals(hdVar.c())) {
            throw new ex("request url is empty");
        }
    }

    public hf a(hd hdVar, boolean z) throws ex {
        ex e;
        Proxy proxy = null;
        try {
            c(hdVar);
            if (hdVar.h != null) {
                proxy = hdVar.h;
            }
            return new ha(hdVar.f, hdVar.g, proxy, z).a(hdVar.k(), hdVar.a(), hdVar.l());
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new ex("未知的错误");
        }
    }
}
